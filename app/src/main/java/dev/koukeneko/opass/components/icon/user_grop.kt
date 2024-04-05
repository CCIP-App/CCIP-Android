import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
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
fun user_grop(): ImageVector {
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
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(10f, 13f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 4f, 0f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 0f)
			}
			path(
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(8f, 21f)
				verticalLineToRelative(-1f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, -2f)
				horizontalLineToRelative(4f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2f)
				verticalLineToRelative(1f)
			}
			path(
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(15f, 5f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 4f, 0f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 0f)
			}
			path(
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(17f, 10f)
				horizontalLineToRelative(2f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2f)
				verticalLineToRelative(1f)
			}
			path(
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(5f, 5f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 4f, 0f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 0f)
			}
			path(
				fill = null,
				fillAlpha = 1.0f,
				stroke = SolidColor(Color(0xFF2C3E50)),
				strokeAlpha = 1.0f,
				strokeLineWidth = 1.5f,
				strokeLineCap = StrokeCap.Round,
				strokeLineJoin = StrokeJoin.Round,
				strokeLineMiter = 1.0f,
				pathFillType = PathFillType.NonZero
			) {
				moveTo(3f, 13f)
				verticalLineToRelative(-1f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, -2f)
				horizontalLineToRelative(2f)
			}
		}.build()
	}
}

