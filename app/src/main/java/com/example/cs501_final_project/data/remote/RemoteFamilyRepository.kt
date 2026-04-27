package com.example.cs501_final_project.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RemoteFamilyRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun now(): Long {
        return System.currentTimeMillis()
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first.")
    }

    private fun currentEmail(): String {
        return auth.currentUser?.email ?: ""
    }

    private fun currentName(): String {
        val user = auth.currentUser
        return user?.displayName ?: user?.email ?: "User"
    }

    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun currentUserEmail(): String {
        return currentEmail()
    }

    private suspend fun saveUserDocument(
        user: FirebaseUser,
        nameFromInput: String? = null,
        isNewUser: Boolean = false
    ) {
        val time = now()
        val userRef = db.collection("users").document(user.uid)
        val snapshot = userRef.get().await()

        val finalName = when {
            !nameFromInput.isNullOrBlank() -> nameFromInput.trim()
            !user.displayName.isNullOrBlank() -> user.displayName ?: "User"
            !user.email.isNullOrBlank() -> user.email ?: "User"
            else -> "User"
        }

        val email = user.email ?: ""

        if (!snapshot.exists() || isNewUser) {
            val userData = mapOf(
                "uid" to user.uid,
                "email" to email,
                "name" to finalName,
                "birthday" to "",
                "phone" to "",
                "selectedFamilyId" to "",
                "familyIds" to emptyList<String>(),
                "createdAt" to time,
                "lastLoginAt" to time,
                "updatedAt" to time
            )

            userRef.set(userData, SetOptions.merge()).await()
        } else {
            val updateData = mapOf(
                "uid" to user.uid,
                "email" to email,
                "name" to finalName,
                "lastLoginAt" to time,
                "updatedAt" to time
            )

            userRef.set(updateData, SetOptions.merge()).await()
        }
    }

    private suspend fun ensureUserDocument() {
        val user = auth.currentUser ?: return
        saveUserDocument(user = user, isNewUser = false)
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String
    ) {
        val cleanEmail = email.trim()
        val cleanName = name.trim().ifBlank { "User" }

        val result = auth.createUserWithEmailAndPassword(
            cleanEmail,
            password
        ).await()

        val user = result.user ?: return

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(cleanName)
            .build()

        user.updateProfile(profileUpdate).await()

        saveUserDocument(
            user = user,
            nameFromInput = cleanName,
            isNewUser = true
        )
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        ensureUserDocument()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun createFamily(familyName: String): String {
        ensureUserDocument()

        val uid = currentUid()
        val time = now()

        val familyRef = db.collection("families").document()
        val userRef = db.collection("users").document(uid)

        val cleanFamilyName = familyName.trim().ifBlank {
            "${currentName()}'s Family"
        }

        val family = CloudFamily(
            id = familyRef.id,
            familyName = cleanFamilyName,
            ownerUid = uid,
            memberUids = listOf(uid),
            roles = mapOf(uid to "owner"),
            createdAt = time,
            updatedAt = time
        )

        familyRef.set(family).await()

        val memberRef = familyRef.collection("members").document()

        val meMember = CloudFamilyMember(
            id = memberRef.id,
            familyId = familyRef.id,
            name = currentName().ifBlank { "Me" },
            relationship = "Me",
            birthday = "",
            linkedUserUid = uid,
            createdByUid = uid,
            createdAt = time,
            updatedAt = time
        )

        memberRef.set(meMember).await()

        userRef.set(
            mapOf(
                "selectedFamilyId" to familyRef.id,
                "familyIds" to FieldValue.arrayUnion(familyRef.id),
                "lastLoginAt" to time,
                "updatedAt" to time
            ),
            SetOptions.merge()
        ).await()

        return familyRef.id
    }

    fun listenMyFamilies(): Flow<List<CloudFamily>> = callbackFlow {
        val user = auth.currentUser

        if (user == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = db.collection("families")
            .whereArrayContains("memberUids", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val families = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudFamily::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(families)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun addFamilyMember(
        familyId: String,
        name: String,
        relationship: String,
        birthday: String
    ) {
        ensureUserDocument()

        val uid = currentUid()
        val time = now()

        val memberRef = db.collection("families")
            .document(familyId)
            .collection("members")
            .document()

        val member = CloudFamilyMember(
            id = memberRef.id,
            familyId = familyId,
            name = name.trim(),
            relationship = relationship.trim(),
            birthday = birthday.trim(),
            linkedUserUid = "",
            createdByUid = uid,
            createdAt = time,
            updatedAt = time
        )

        memberRef.set(member).await()
    }

    fun listenFamilyMembers(familyId: String): Flow<List<CloudFamilyMember>> = callbackFlow {
        val listener = db.collection("families")
            .document(familyId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudFamilyMember::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.createdAt } ?: emptyList()

                trySend(members)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun addHealthRecord(
        familyId: String,
        memberId: String,
        type: String,
        title: String,
        symptoms: String,
        bodyArea: String,
        painLevel: Int,
        urgency: String,
        notes: String
    ) {
        ensureUserDocument()

        val uid = currentUid()
        val time = now()

        val recordRef = db.collection("families")
            .document(familyId)
            .collection("records")
            .document()

        val record = CloudHealthRecord(
            id = recordRef.id,
            familyId = familyId,
            memberId = memberId,
            type = type.trim(),
            title = title.trim(),
            symptoms = symptoms.trim(),
            bodyArea = bodyArea.trim(),
            painLevel = painLevel,
            urgency = urgency.trim(),
            notes = notes.trim(),
            createdByUid = uid,
            createdAt = time,
            updatedAt = time
        )

        recordRef.set(record).await()
    }

    fun listenHealthRecords(
        familyId: String,
        memberId: String? = null
    ): Flow<List<CloudHealthRecord>> = callbackFlow {
        var query = db.collection("families")
            .document(familyId)
            .collection("records")
            .limit(100)

        if (!memberId.isNullOrBlank()) {
            query = query.whereEqualTo("memberId", memberId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val records = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(CloudHealthRecord::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.createdAt } ?: emptyList()

            trySend(records)
        }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun requestToJoinFamily(familyId: String) {
        ensureUserDocument()

        val uid = currentUid()
        val time = now()
        val cleanFamilyId = familyId.trim()

        val requestRef = db.collection("families")
            .document(cleanFamilyId)
            .collection("joinRequests")
            .document(uid)

        val request = CloudJoinRequest(
            id = uid,
            familyId = cleanFamilyId,
            requesterUid = uid,
            requesterEmail = currentEmail(),
            requesterName = currentName(),
            status = "pending",
            createdAt = time,
            updatedAt = time
        )

        requestRef.set(request).await()
    }

    fun listenJoinRequests(familyId: String): Flow<List<CloudJoinRequest>> = callbackFlow {
        val listener = db.collection("families")
            .document(familyId)
            .collection("joinRequests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudJoinRequest::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.createdAt } ?: emptyList()

                trySend(requests)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun approveJoinRequest(
        familyId: String,
        request: CloudJoinRequest,
        role: String = "editor"
    ) {
        val time = now()

        val familyRef = db.collection("families").document(familyId)
        val requestRef = familyRef
            .collection("joinRequests")
            .document(request.requesterUid)

        db.runBatch { batch ->
            batch.update(
                familyRef,
                "memberUids",
                FieldValue.arrayUnion(request.requesterUid)
            )

            batch.update(
                familyRef,
                "updatedAt",
                time
            )

            batch.update(
                familyRef,
                FieldPath.of("roles", request.requesterUid),
                role
            )

            batch.update(
                requestRef,
                mapOf(
                    "status" to "accepted",
                    "updatedAt" to time
                )
            )
        }.await()
    }

    suspend fun rejectJoinRequest(
        familyId: String,
        request: CloudJoinRequest
    ) {
        val requestRef = db.collection("families")
            .document(familyId)
            .collection("joinRequests")
            .document(request.requesterUid)

        requestRef.update(
            mapOf(
                "status" to "rejected",
                "updatedAt" to now()
            )
        ).await()
    }
}