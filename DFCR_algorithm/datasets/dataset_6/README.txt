DS06_multihop_chain
===================

Mục đích
- Kiểm thử bootstrapping hop-count và routing multi-hop dài.

Cách dùng
  - Chạy DFCR và in hop-count của gateways 15000..15009 sau bootstrapping.
  - Theo dõi đường đi gói tin từ gateway xa nhất (15009) về BS.

Kỳ vọng/Assert
  - Hop-count tăng dần theo chuỗi (15000 gần BS nhất).
  - Packet từ các gateway xa phải qua nhiều hop (>=3), không bị loop.
