package dev.koukeneko.opass.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cube

@Composable
fun PanelBtn(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    PanelBtnBase(
        title = title,
        onClick = onClick,
        color = color
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun PanelBtn(
    title: String,
    iconUrl: String, // for AsyncImage
    onClick: () -> Unit,
    color: Color
) {
    PanelBtnBase(
        title = title,
        onClick = onClick,
        color = color
    ) {
        // if iconUrl is empty, use default icon
        if (iconUrl.isEmpty()){
            Icon(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                imageVector = cube(),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }else {
            AsyncImage(
                model = iconUrl,
                contentDescription = title,
                modifier = Modifier
                    .height(30.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
        }

    }
}

@Composable
private fun PanelBtnBase(
    title: String,
    onClick: () -> Unit,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(color = color, shape = MaterialTheme.shapes.medium)
                .padding(5.dp)
        ) {
            content()
        }
        Spacer(modifier = Modifier.padding(2.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

