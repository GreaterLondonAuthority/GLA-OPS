/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question

import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.project.template.domain.AnswerType
import java.time.LocalDate
import java.time.format.DateTimeParseException

const val SHORT_TEXT_LENGTH = 80

fun validateAnswer(answer: Answer) {
    // answers can be optional
    if (answer.answer == null || answer.answer.isEmpty()) {
        return
    }
    when (answer.question.answerType) {
        AnswerType.Date -> validateDate(answer.answer)
        AnswerType.YesNo -> validateBoolean(answer.answer)
        AnswerType.Text -> validateText(answer.answer, SHORT_TEXT_LENGTH)
        AnswerType.FreeText -> validateText(answer.answer, answer.question.maxLength)
        AnswerType.Dropdown -> validateDropdown(answer.answer, answer.question.maxAnswers, answer.question.delimiter)
        else -> throw RuntimeException("Unrecognised answer type: " + answer.question.answerType)
    }
}

fun validateBoolean(answer: String) {
    if (!(answer.equals("yes", ignoreCase = true) || answer.equals("no", ignoreCase = true))) {
        throw ValidationException("answer", "Acceptable values for answer are: Yes/No only")
    }
}

fun validateDate(answer: String?) {
    try {
        LocalDate.parse(answer)
    } catch (e: DateTimeParseException) {
        throw ValidationException("answer", "Unable to format the given value as a date.")
    }
}

fun validateDropdown(answer: String?, maxAnswers: Int?, delimiter: String?) {
    if (answer == null || maxAnswers == null || delimiter == null) {
        return
    }
    if (answer.split(delimiter.toRegex()).toTypedArray().size > maxAnswers) {
        throw ValidationException("answer", String.format("The maximum allowed answers amount is %d.", maxAnswers))
    }
}

fun validateText(answer: String, maxLenth: Int) {
    if (answer.length > maxLenth) {
        throw ValidationException("answer", String.format("The maximum length for the answer field is %d characters.", maxLenth))
    }

    // TODO: check that short text answers don't contain newlines
}
