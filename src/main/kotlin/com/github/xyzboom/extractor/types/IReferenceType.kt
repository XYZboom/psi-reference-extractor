package com.github.xyzboom.extractor.types

sealed interface IReferenceType
sealed interface IReferenceTargetType : IReferenceType
sealed interface IReferenceSourceType : IReferenceType

data object Unknown : IReferenceSourceType, IReferenceTargetType
data object Import : IReferenceSourceType, IReferenceTargetType
data object Call : IReferenceSourceType
data object Property : IReferenceSourceType, IReferenceTargetType
data object Method : IReferenceTargetType