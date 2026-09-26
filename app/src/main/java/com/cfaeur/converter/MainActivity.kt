package com.cfaeur.converter

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var amountInput: EditText
    private lateinit var directionButton: Button
    private lateinit var resultView: TextView
    private lateinit var inputCurrency: TextView
    private lateinit var outputCurrency: TextView
    private var direction = Direction.CFA_TO_EUR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountInput = findViewById(R.id.amount_input)
        directionButton = findViewById(R.id.direction_button)
        resultView = findViewById(R.id.result_value)
        inputCurrency = findViewById(R.id.input_currency)
        outputCurrency = findViewById(R.id.output_currency)

        if (savedInstanceState != null) {
            direction = Direction.valueOf(
                savedInstanceState.getString("direction", Direction.CFA_TO_EUR.name)
            )
            amountInput.setText(savedInstanceState.getString("amount", "0"))
        } else {
            amountInput.setText("0")
        }

        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = render()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        directionButton.setOnClickListener {
            val amount = MoneyConverter.parse(amountInput.text.toString(), direction)
            val converted = amount?.let { MoneyConverter.convert(it, direction) }
            direction = direction.reversed()
            if (converted != null) {
                amountInput.setText(converted.stripTrailingZeros().toPlainString().replace('.', ','))
                amountInput.setSelection(amountInput.length())
            }
            render()
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("direction", direction.name)
        outState.putString("amount", amountInput.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun render() {
        inputCurrency.text = direction.source
        outputCurrency.text = direction.target
        directionButton.contentDescription = getString(R.string.swap_description)
        val amount = MoneyConverter.parse(amountInput.text.toString(), direction)
        resultView.text = amount?.let {
            MoneyConverter.format(MoneyConverter.convert(it, direction), direction.target)
        } ?: "—"
    }
}
