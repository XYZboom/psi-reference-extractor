package com.github.xyzboom.extractor

import picocli.CommandLine
import kotlin.system.exitProcess


fun main(args: Array<String>): Unit = exitProcess(CommandLine(RefExtract()).execute(*args))