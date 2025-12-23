import pandas as pd
import matplotlib.pyplot as plt

# ===== 1. Đọc dữ liệu từ CSV =====
csv_path = csv_path = r"D:\HANOI_UNIVERSITY_OF_INDUSTRY\Year_3rd\TTCSN\CODE_DEMO\DFCR_algorithm\results\dfcr_dead_chs_wsn2.csv"

df = pd.read_csv(csv_path)

# ===== 2. Vẽ biểu đồ =====
plt.figure(figsize=(7, 5))
plt.plot(df["round"], df["deadCH"], linewidth=2, marker="o")

# ===== 3. Gán nhãn & tiêu đề =====
plt.xlabel("Round", fontsize=12)
plt.ylabel("Number of Dead Cluster Heads", fontsize=12)
plt.title("Dead Cluster Heads vs Round (DFCR)", fontsize=13)

# ===== 4. Hiển thị lưới =====
plt.grid(True, linestyle="--", alpha=0.6)

# ===== 5. Căn layout =====
plt.tight_layout()

# ===== 6. Lưu ảnh (đưa vào báo cáo) =====
plt.savefig("dead_ch_vs_round.png", dpi=300)

# ===== 7. Hiển thị =====
plt.show()
