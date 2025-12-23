import pandas as pd
import matplotlib.pyplot as plt


def main():
    # ===== 1. Đọc file CSV =====
    file_path = r"D:\HANOI_UNIVERSITY_OF_INDUSTRY\Year_3rd\TTCSN\CODE_DEMO\DFCR_algorithm\results\alive_sensors_wsn1.csv"
    df = pd.read_csv(file_path)

    print("=== DỮ LIỆU ĐỌC ĐƯỢC ===")
    print(df.head())

    # ===== 2. Kiểm tra cột =====
    # Giả sử cột là: round và alive_sensors
    if "round" not in df.columns:
        raise ValueError("Không tìm thấy cột 'round' trong file CSV")

    # Tìm cột alive (linh hoạt tên)
    alive_col = None
    for col in df.columns:
        if "alive" in col.lower():
            alive_col = col
            break

    if alive_col is None:
        raise ValueError("Không tìm thấy cột số sensor còn sống")

    # ===== 3. Vẽ biểu đồ =====
    plt.figure(figsize=(10, 6))
    plt.plot(df["round"], df[alive_col], linewidth=2)

    plt.xlabel("Round")
    plt.ylabel("Alive Sensors")
    plt.title("Number of Alive Sensors over Rounds (WSN#1)")

    plt.grid(True)
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()
