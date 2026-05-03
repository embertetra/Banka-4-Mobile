package rs.raf.banka4mobile.domain.model.transfer

data class TransferHistory(
    val transfers: List<Transfer>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)