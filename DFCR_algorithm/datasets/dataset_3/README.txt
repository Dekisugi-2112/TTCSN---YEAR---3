DS03_isolated_uncoset
=====================

Mục đích
- Kiểm thử sensor UnCOset bị cô lập (BackupSet rỗng) => phải trở thành inactive.

Cách dùng
  - Chạy DFCR bình thường.
  - Theo dõi các sensor id 190..199 (10 node) trong log.

Kỳ vọng/Assert
  - Các sensor id 190..199 thuộc UnCOset và BackupSet(si) rỗng.
  - Chúng phải bị đánh dấu inactive (còn năng lượng nhưng không giao tiếp được), không gây loop/treo chương trình.
  - Các sensor còn lại vẫn hoạt động bình thường.
