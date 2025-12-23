DS08_ch_dies_early_recover
==========================

Mục đích
- Kiểm thử fault-tolerant clustering: CH chết sớm, member phải tái bám CH khác/relay.

Cách dùng
  - Chạy DFCR đủ nhiều round để gateway năng lượng thấp chết (xem gateways.csv, dòng #6).
  - Theo dõi cluster membership của các sensor quanh gateway chết.

Kỳ vọng/Assert
  - Khi CH chết, member phát hiện lỗi và re-attach sang CH khác hoặc qua relay.
  - Số inactive sensors tăng ít (vì vẫn có CH lân cận trong range).
