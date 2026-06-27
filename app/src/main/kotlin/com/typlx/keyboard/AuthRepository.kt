package com.typlx.keyboard

/**
 * Contract for user authentication.
 *
 * The production implementation will use Supabase Auth (as recommended in TYP-145).
 * The stub ([StubAuthRepository]) is the active implementation until TYP-124 is
 * approved and a Supabase project URL + anon key are available.
 *
 * Post-approval wiring checklist:
 * 1. Add Supabase Kotlin client to build.gradle.kts:
 *      implementation("io.github.jan-tennert.supabase:auth-kt:2.x.x")
 * 2. Create [SupabaseAuthRepository] implementing this interface.
 * 3. Wire it as the active implementation in the DI graph (or companion factory).
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
 * Stub implementation — no real auth. Used until Supabase is configured.
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
