package rs.raf.banka4mobile.data.remote.dto

import rs.raf.banka4mobile.domain.model.orders.Order
import rs.raf.banka4mobile.domain.model.orders.OrdersPage

fun OrderDto.toDomain(): Order {
    return Order(
        id = orderId,
        orderType = orderType,
        ticker = ticker,
        listingName = listingName,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        status = status,
        createdAt = createdAt,
        executionDate = executionDate,
        commissionCharged = commissionCharged,
        assetType = assetType,
        filledQuantity = filledQuantity,
        remainingQuantity = remainingQuantity,
        isDone = isDone,
        isPartialFill = isPartialFill,
        isCancelled = isCancelled,
        cancelReason = cancelReason
    )
}

fun OrdersResponseDto.toDomain(): OrdersPage {
    return OrdersPage(
        orders = data.map { it.toDomain() },
        page = page,
        pageSize = pageSize,
        total = total,
        totalPages = totalPages ?: 1
    )
}