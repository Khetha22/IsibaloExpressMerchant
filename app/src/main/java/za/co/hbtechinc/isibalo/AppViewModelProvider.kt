package za.co.hbtechinc.isibalo

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import za.co.hbtechinc.isibalo.ui.assistant.AssistantViewModel
import za.co.hbtechinc.isibalo.ui.pos.PosViewModel
import za.co.hbtechinc.isibalo.ui.sales.SalesViewModel
import za.co.hbtechinc.isibalo.ui.stock.StockViewModel

fun CreationExtras.app(): IsibaloApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as IsibaloApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { PosViewModel(app().container.repository) }
        initializer { SalesViewModel(app(), app().container.repository) }
        initializer { StockViewModel(app().container.repository) }
        initializer { AssistantViewModel(app(), app().container.repository, app().container.ai) }
    }
}
