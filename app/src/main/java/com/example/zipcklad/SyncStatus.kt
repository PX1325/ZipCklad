sealed class SyncStatus {
    object IDLE : SyncStatus()
    object LOADING : SyncStatus()
    object SUCCESS : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}