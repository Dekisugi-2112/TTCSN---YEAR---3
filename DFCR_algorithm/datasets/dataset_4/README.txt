DS04_chcost_choice
==================

Mục đích
- Kiểm thử logic chọn CH theo CHCost (không đơn thuần chọn CH gần nhất).

Cách dùng
  - Chạy DFCR và in quyết định chọn CH của sensor id=0.
  - Sensor id=0 nhìn thấy 2 gateway: 13000 và 13001 (đều trong 50m).

Kỳ vọng/Assert
  - Sensor id=0 phải ưu tiên gateway 13001 (energy cao + gần BS hơn) theo CHCost.
  - Nếu code đang chọn 'nearest CH' thì dataset này sẽ làm lộ bug.
