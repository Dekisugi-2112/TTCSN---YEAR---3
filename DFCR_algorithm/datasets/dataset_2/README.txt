DS02_mixed_coverage_relay
=========================

Mục đích
- Kiểm thử UnCOset nhưng có BackupSet (relay) để vẫn gửi được về CH/BS.

Cách dùng
  - Chạy DFCR bình thường.
  - Bật log/print: COset, UnCOset, BackupSet(si) của các sensor uncovered.

Kỳ vọng/Assert
  - Có một phần sensor thuộc UnCOset ngay round đầu.
  - Mọi sensor UnCOset đều chọn được relay (BackupSet không rỗng) và dữ liệu vẫn đến CH/BS qua đường gián tiếp.
  - Không có sensor bị inactive do 'cô lập'.
