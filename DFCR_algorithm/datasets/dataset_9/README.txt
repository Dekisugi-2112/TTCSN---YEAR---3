DS09_multihop_reroute_after_failure
===================================

Mục đích
- Kiểm thử routing fault tolerance: CH trung gian chết → phải reroute qua nhánh song song.

Cách dùng
  - Chạy DFCR đủ lâu để gateway 18003 chết (energy rất thấp).
  - Theo dõi next-hop của các gateway phía sau 18003 (xa BS).

Kỳ vọng/Assert
  - Sau khi 18003 chết, route phải chuyển sang chainB (nhờ cross-link trong range).
  - Không xảy ra loop; hop-count/route cập nhật lại hợp lệ.
