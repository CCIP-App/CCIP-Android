import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
fun puzzle(): ImageVector {
    return remember {
        ImageVector.Builder(
                name = "Untitled1",
                defaultWidth = 44.dp,
                defaultHeight = 44.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
				path(
    				fill = null,
    				fillAlpha = 1.0f,
    				stroke = null,
    				strokeAlpha = 1.0f,
    				strokeLineWidth = 1.5f,
    				strokeLineCap = StrokeCap.Round,
    				strokeLineJoin = StrokeJoin.Round,
    				strokeLineMiter = 1.0f,
    				pathFillType = PathFillType.NonZero
				) {
					moveTo(0f, 0f)
					horizontalLineToRelative(24f)
					verticalLineToRelative(24f)
					horizontalLineTo(0f)
					close()
}
				path(
    				fill = SolidColor(Color(0xFF000000)),
    				fillAlpha = 1.0f,
    				stroke = SolidColor(Color(0xFF2C3E50)),
    				strokeAlpha = 1.0f,
    				strokeLineWidth = 0f,
    				strokeLineCap = StrokeCap.Round,
    				strokeLineJoin = StrokeJoin.Round,
    				strokeLineMiter = 1.0f,
    				pathFillType = PathFillType.NonZero
				) {
					moveTo(10f, 2f)
					arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.995f, 2.824f)
					lineToRelative(0.005f, 0.176f)
					verticalLineToRelative(1f)
					horizontalLineToRelative(3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.995f, 1.85f)
					lineToRelative(0.005f, 0.15f)
					verticalLineToRelative(3f)
					horizontalLineToRelative(1f)
					arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.176f, 5.995f)
					lineToRelative(-0.176f, 0.005f)
					horizontalLineToRelative(-1f)
					verticalLineToRelative(3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.85f, 1.995f)
					lineToRelative(-0.15f, 0.005f)
					horizontalLineToRelative(-3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.995f, -1.85f)
					lineToRelative(-0.005f, -0.15f)
					verticalLineToRelative(-1f)
					arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.993f, -0.117f)
					lineToRelative(-0.007f, 0.117f)
					verticalLineToRelative(1f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.85f, 1.995f)
					lineToRelative(-0.15f, 0.005f)
					horizontalLineToRelative(-3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.995f, -1.85f)
					lineToRelative(-0.005f, -0.15f)
					verticalLineToRelative(-3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.85f, -1.995f)
					lineToRelative(0.15f, -0.005f)
					horizontalLineToRelative(1f)
					arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.117f, -1.993f)
					lineToRelative(-0.117f, -0.007f)
					horizontalLineToRelative(-1f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.995f, -1.85f)
					lineToRelative(-0.005f, -0.15f)
					verticalLineToRelative(-3f)
					arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.85f, -1.995f)
					lineToRelative(0.15f, -0.005f)
					horizontalLineToRelative(3f)
					verticalLineToRelative(-1f)
					arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, -3f)
					close()
}
}.build()
    }
}

