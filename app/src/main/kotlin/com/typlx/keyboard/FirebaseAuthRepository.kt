package com.typlx.keyboard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun currentUser(): AuthUser? {
        return auth.currentUser?.toAuthUser()
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toAuthUser()
                ?: return AuthResult.Error("Sign-in succeeded but user is null")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-in failed")
        }
    }

    override suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.toAuthUser()
                ?: return AuthResult.Error("Sign-up succeeded but user is null")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-up failed")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    suspend fun getIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            id = uid,
            email = email ?: "",
        )
    }
}
