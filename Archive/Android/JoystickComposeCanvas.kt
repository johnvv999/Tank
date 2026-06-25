@Composable
fun Joystick(
    size: Dp,
    onMove: (Float, Float) -> Unit
) {
    val radius = with(LocalDensity.current) { size.toPx() / 2 }
    var handleX by remember { mutableStateOf(0f) }
    var handleY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()

                        handleX = (handleX + dragAmount.x).coerceIn(-radius, radius)
                        handleY = (handleY + dragAmount.y).coerceIn(-radius, radius)

                        val normX = handleX / radius
                        val normY = -handleY / radius  // invert Y for natural control

                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        handleX = 0f
                        handleY = 0f
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        // Outer ring
        drawCircle(
            color = Color.DarkGray,
            radius = radius,
            center = center
        )

        // Inner handle
        drawCircle(
            color = Color.Green,
            radius = radius / 4,
            center = Offset(center.x + handleX, center.y + handleY)
        )
    }
}
