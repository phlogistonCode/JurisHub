package dev.phlogiston.jurishub.core

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun chooseFile(): File? {
    var selected: File? = null
    val dialog = object : FileDialog(null as Frame?, "Выберите документ", LOAD) {
        override fun setVisible(value: Boolean) {
            super.setVisible(value)
            if (value) selected = files.firstOrNull()
        }
    }
    dialog.isVisible = true
    return selected
}