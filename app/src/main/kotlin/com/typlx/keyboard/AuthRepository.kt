package com.typlx.keyboard

/**
 * Contract for user authentication.
 *
 * The production implementation uses Firebase Auth ([FirebaseAuthRepository]).
 * The stub ([StubAuthRepository]) remains the active implementation until the
 * Firebase project is created (TYP-546) and google-services.json is available.
 *
 * Activation checklist:
 * 1. Download google-services.json from Firebase console into app/.
 * 2. Switch the active implementation from [StubAuthRepository] to [FirebaseAuthRepository].
 */
interface AuthRepository {
    /** The currently authenticated user, or null if not signed in. */
    suspend fun currentUser(): AuthUser?

    /** Sign in with email and password. */
    suspend fun signIn(email: String, password: String): AuthResult

    /** Sign up with email and password. */
    suspend fun signUp(email: String, password: String): AuthResult

    /** Sign out the current user. */
    suspend fun signOut()

    /** True if a user is currently authenticated. */
    suspend fun isSignedIn(): Boolean = currentUser() != null
}

data class AuthUser(
    val id: String,
    val email: String,
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Stub implementation — no real auth. Used until Firebase is configured.
 *
 * Always reports no user signed in. This keeps the app fully functional
 * in passthrough mode while [FeatureGate] allows all features regardless.
 */
class StubAuthRepository : AuthRepository {
    override suspend fun currentUser(): AuthUser? = null
    override suspend fun signIn(email: String, password: String): AuthResult =
        AuthResult.Error("Authentication not yet configured")
    override suspend fun signUp(email: String, password: String): AuthResult =
        AuthResult.Error("Authentication not yet configured")
    override suspend fun signOut() = Unit
}
