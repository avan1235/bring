package `in`.procyk.savvry.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExposureNeg1
import androidx.compose.material.icons.outlined.ExposurePlus1
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.savvry.ui.SavvryAppTheme

@Composable
internal inline fun NumberRow(
    value: Int,
    crossinline onValueChange: (Int) -> Unit,
    minVisible: Int = 1,
    modifier: Modifier = Modifier,
    before: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        before()
        AnimatedVisibilityGhostButton(
            visible = value > minVisible - 1,
            icon = Icons.Outlined.ExposureNeg1,
            onClick = { onValueChange(value - 1) },
        )
        Text(
            text = value.toString(),
            textAlign = TextAlign.Center,
            style = SavvryAppTheme.typography.input,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    bottom = 5.dp,
                    start = 2.dp,
                    end = 2.dp,
                )
                .border(2.dp, SavvryAppTheme.colors.primary, RoundedCornerShape(8.dp))
                .padding(
                    top = 4.dp,
                    bottom = 5.dp,
                    start = 4.dp,
                    end = 4.dp,
                )
                .width(32.dp),
        )
        AnimatedVisibilityGhostButton(
            onClick = { onValueChange(value + 1) },
            icon = Icons.Outlined.ExposurePlus1,
        )
    }
}
