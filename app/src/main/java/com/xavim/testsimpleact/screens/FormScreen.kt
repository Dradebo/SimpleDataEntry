package com.xavim.testsimpleact.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun  FormScreen() {
    Spacer(Modifier.size(Spacing.Spacing40))
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var surname by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var age by remember { mutableStateOf(TextFieldValue("")) }

    InputText(
        modifier = Modifier.padding(bottom = Spacing.Spacing16),
        state = InputShellState.UNFOCUSED,
        title = "Name",
        inputTextFieldValue = name,
        onValueChanged = {
            if (it != null) {
                name = it
            }
        },
    )

    InputText(
        modifier = Modifier.padding(bottom = Spacing.Spacing16),

        state = InputShellState.UNFOCUSED,
        title = "Surname",
        inputTextFieldValue = name,
        onValueChanged = {
            if (it != null) {
                surname = it
            }
        },
    )

    InputText(
        modifier = Modifier.padding(bottom = Spacing.Spacing16),

        state = InputShellState.UNFOCUSED,
        title = "Email",
        inputTextFieldValue = email,
        onValueChanged = {
            email
        },
    )

    InputText(
        modifier = Modifier.padding(bottom = Spacing.Spacing16),

        state = InputShellState.UNFOCUSED,
        title = "Address",
        inputTextFieldValue = address,
        onValueChanged = {
            if (it != null) {
                address = it
            }
        },
    )
    var ageState by remember { mutableStateOf(InputShellState.UNFOCUSED) }
    InputNumber(
        modifier = Modifier.padding(bottom = Spacing.Spacing16),
        state = ageState,
        title = "Age",
        inputTextFieldValue = name,
        onValueChanged = {
            if (it != null) {
                age = it
                ageState = getAgeInputState(it.text.toInt())
            }
        },
        supportingText = getAgeInputSupportingText(ageState)
    )
}

data class FormScreenState (
       val name: String,
        val  email: String,
)


fun getAgeInputSupportingText(ageState: InputShellState): List<SupportingTextData>? {
    return when(ageState) {
        InputShellState.ERROR -> listOf(SupportingTextData("Age must be between 18 and 120", SupportingTextState.ERROR))
        else -> null
    }
}

fun getAgeInputState(value: Int): InputShellState {
        return when(value) {
            in 0..17 ->  InputShellState.ERROR
            in 18..120 ->  InputShellState.FOCUSED
            else ->  InputShellState.FOCUSED
        }
}
