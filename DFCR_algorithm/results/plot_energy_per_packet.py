import pandas as pd
import matplotlib.pyplot as plt

# ===== Đọc CSV =====
csv_path = r"D:\HANOI_UNIVERSITY_OF_INDUSTRY\Year_3rd\TTCSN\CODE_DEMO\DFCR_algorithm\results\energy_per_packet_wsn2.csv"
df = pd.read_csv(csv_path)

# ===== Vẽ scatter =====
plt.figure(figsize=(7, 5))
plt.scatter(
    range(len(df)),
    df["energy"],
    s=10,
    alpha=0.6
)

plt.xlabel("Packet Index")
plt.ylabel("Energy per Packet (J)")
plt.title("Energy Consumption per Packet (DFCR)")

plt.grid(True, linestyle="--", alpha=0.5)
plt.tight_layout()

plt.savefig("energy_per_packet.png", dpi=300)
plt.show()


