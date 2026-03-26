package com.skylens.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor() {

    private val supabaseUrl = "https://qmmxycrgdzburefqemxl.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFtbXh5Y3JnZHpidXJlZnFlbXhsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ1NDczNTksImV4cCI6MjA5MDEyMzM1OX0.bgyg7mSwAqM1qAkJzozM31Of6vI64cPmhxrUgtMJTpc"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
}
