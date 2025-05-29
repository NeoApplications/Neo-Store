package com.machiav3lli.fdroid.manager.installer

sealed class InstallationError(message: String) : Exception(message) {
    class UserCancelled : InstallationError("Installation was cancelled by the user")
    class Timeout : InstallationError("Installation timed out")
    class InsufficientStorage : InstallationError("Insufficient storage space")
    class Incompatible : InstallationError("Incompatible app")
    class Blocked : InstallationError("Installation was blocked by the system")
    class ConflictingSignature :
        InstallationError("The app signature conflicts with an installed app")

    class Downgrade : InstallationError("Downgrade is not allowed")
    class RootAccessDenied : InstallationError("Root access was denied")
    class PackageInvalid : InstallationError("Invalid APK package")
    class Unknown(message: String = "Unknown error") : InstallationError(message)
}

