package com.github.xyzboom.extractor

import picocli.CommandLine
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val refExtract = RefExtract()
    exitProcess(CommandLine(refExtract, refExtract).execute(*args))
}