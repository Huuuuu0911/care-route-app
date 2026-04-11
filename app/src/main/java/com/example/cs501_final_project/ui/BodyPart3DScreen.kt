package com.example.cs501_final_project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501_final_project.ui.components.AppButton
import com.example.cs501_final_project.ui.components.AppCard
import io.github.sceneview.Scene
import io.github.sceneview.collision.Vector3
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine

@Composable
fun BodyPart3DScreen(
    onBodyPartSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = ModelLoader(engine)

    val modelNode = ModelNode(
        modelInstance = modelLoader.createModelInstance("models/male_model.glb"),
        scaleToUnits = 1.5f,
        centerOrigin = Vector3(0.0f, 1.0f, 0.0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "3D Body Viewer",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AppCard {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Rotate the model and choose a body area",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Scene(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    engine = engine,
                    childNodes = listOf(modelNode)
                )
            }
        }

        AppCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppButton(
                    text = "Head",
                    onClick = { onBodyPartSelected("Head") }
                )

                AppButton(
                    text = "Chest",
                    onClick = { onBodyPartSelected("Chest") }
                )

                AppButton(
                    text = "Stomach",
                    onClick = { onBodyPartSelected("Stomach") }
                )

                AppButton(
                    text = "Back",
                    onClick = { onBodyPartSelected("Back") }
                )

                AppButton(
                    text = "Leg",
                    onClick = { onBodyPartSelected("Leg") }
                )

                AppButton(
                    text = "Back to Home",
                    onClick = onBackClick
                )
            }
        }
    }
}