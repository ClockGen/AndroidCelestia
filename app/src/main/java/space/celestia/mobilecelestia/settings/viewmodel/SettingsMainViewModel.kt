package space.celestia.mobilecelestia.settings.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.mobilecelestia.purchase.PurchaseManager
import javax.inject.Inject

@HiltViewModel
class SettingsMainViewModel
@Inject constructor(val purchaseManager: PurchaseManager) : ViewModel()