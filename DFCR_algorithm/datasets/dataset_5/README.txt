DS05_routing_branch_loadbalance
===============================

Mục đích
- Kiểm thử routing next-hop selection + cân bằng tải theo |BackCH| trong Cost.

Cách dùng
  - Chạy DFCR, bật log nextHop của gateway 14007 và 14008.
  - Quan sát route có dồn hết về 1 nhánh hay phân tán khi |BackCH| tăng.

Kỳ vọng/Assert
  - Next hop chỉ được chọn trong các gateway có hop-count nhỏ hơn.
  - Khi BackCH vào một gateway tăng, các gateway xa có xu hướng chọn nhánh khác (nếu code cân bằng tải đúng).
