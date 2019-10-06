package com.sumera.coreactor

import com.sumera.coreactor.contract.EventOrReducer
import kotlinx.coroutines.flow.FlowCollector

typealias CoreactorFlow<STATE> = FlowCollector<EventOrReducer<STATE>>
