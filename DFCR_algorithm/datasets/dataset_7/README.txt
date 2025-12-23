DS07_partition_unreachable
==========================

Mục đích
- Kiểm thử robustness khi một partition gateway/CH không thể reach BS (mạng không liên thông).

Cách dùng
  - Chạy DFCR.
  - Theo dõi hop-count các gateway id 16100..16103.
  - Theo dõi số packet BS nhận từ sensors thuộc vùng partition xa.

Kỳ vọng/Assert
  - Gateway partition xa không thể hội tụ hop-count hữu hạn tới BS (hoặc giữ MAX).
  - Dữ liệu từ sensors vùng partition xa không tới BS (hoặc bị drop), nhưng mô phỏng không crash/treo.
