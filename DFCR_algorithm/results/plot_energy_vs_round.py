import pandas as pd
import matplotlib.pyplot as plt

# ===== 1. Đọc dữ liệu từ CSV =====
csv_path = r"D:\HANOI_UNIVERSITY_OF_INDUSTRY\Year_3rd\TTCSN\CODE_DEMO\DFCR_algorithm\results\energy_per_round_wsn2.csv"
df = pd.read_csv(csv_path)

# ===== 2. Vẽ biểu đồ =====
plt.figure(figsize=(7, 5))
plt.plot(df["round"], df["energyConsumed"], linewidth=2)

# ===== 3. Gán nhãn & tiêu đề =====
plt.xlabel("Round", fontsize=12)
plt.ylabel("Energy Consumed per Round", fontsize=12)
plt.title("Energy Consumption vs Round (DFCR)", fontsize=13)

# ===== 4. Lưới cho dễ nhìn =====
plt.grid(True, linestyle="--", alpha=0.6)

# ===== 5. Căn layout =====
plt.tight_layout()

# ===== 6. Lưu ảnh (để đưa vào báo cáo) =====
plt.savefig("energy_vs_round.png", dpi=300)

# ===== 7. Hiển thị =====
plt.show()
