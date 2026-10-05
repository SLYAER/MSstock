package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.data.repository.InventoryRepository
import com.example.ui.inventory.InventoryScreen
import com.example.ui.inventory.InventoryViewModel
import com.example.ui.staff.StaffLoginScreen
import com.example.ui.theme.ElectroStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElectroStockTheme {
                MSStockApp()
            }
        }
    }
}

@Composable
fun MSStockApp() {
    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val repository = InventoryRepository(app.applicationContext)
                InventoryViewModel(repository)
            }
        }
    )

    val currentStaff by inventoryViewModel.currentStaff.collectAsStateWithLifecycle()
    val staffListState by inventoryViewModel.staffListState.collectAsStateWithLifecycle()

    if (currentStaff == null) {
        StaffLoginScreen(
            staffListState = staffListState,
            onLogin = { staff, enteredPin ->
                inventoryViewModel.loginStaff(staff, enteredPin)
            },
            onRegisterProfile = { name, username, phoneOrEmail, dept ->
                inventoryViewModel.registerStaffProfile(name, username, phoneOrEmail, dept) {}
            },
            onInitializeOwner = {
                inventoryViewModel.initializeMasterOwnerIfEmpty()
            }
        )
    } else {
        InventoryScreen(
            viewModel = inventoryViewModel,
            onSignOut = {
                inventoryViewModel.logoutStaff()
            }
        )
    }
}
