import os
import urllib.request
import subprocess
import sys

URL = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task"
OUTPUT_FILENAME = "gemma3-1b-it-int4.task"
DOWNLOADS_DIR = os.path.expanduser("~\\Downloads")
LOCAL_PATH = os.path.join(DOWNLOADS_DIR, OUTPUT_FILENAME)

def report_progress(block_num, block_size, total_size):
    downloaded = block_num * block_size
    if total_size > 0:
        percent = min(int(downloaded * 100 / total_size), 100)
        sys.stdout.write(f"\r[Downloading Gemma 3 1B Model]: {percent}% ({downloaded / (1024*1024):.1f} MB / {total_size / (1024*1024):.1f} MB)")
        sys.stdout.flush()
    else:
        sys.stdout.write(f"\r[Downloading Gemma 3 1B Model]: {downloaded / (1024*1024):.1f} MB downloaded")
        sys.stdout.flush()

def main():
    print(f"Target download location: {LOCAL_PATH}")
    if os.path.exists(LOCAL_PATH) and os.path.getsize(LOCAL_PATH) > 100_000_000:
        print("\nModel file already exists in Downloads and is valid!")
    else:
        print("Starting download from Hugging Face (approx. 550 MB - 1 GB)...")
        try:
            urllib.request.urlretrieve(URL, LOCAL_PATH, reporthook=report_progress)
            print("\nDownload completed successfully!")
        except Exception as e:
            print(f"\nDownload failed: {e}")
            return

    # Push to HONOR phone via ADB
    print("Checking connected Android devices via ADB...")
    sdk_path = os.path.expanduser("~\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe")
    if not os.path.exists(sdk_path):
        sdk_path = "adb"

    # Push destination for Isibalo Express app
    dest_device_path = "/sdcard/Android/data/za.co.hbtechinc.isibalo/files/model.task"
    print(f"Pushing model to connected phone at {dest_device_path}...")

    cmd = [sdk_path, "push", LOCAL_PATH, dest_device_path]
    res = subprocess.run(cmd)
    if res.returncode == 0:
        print("Successfully pushed Gemma model to your phone! The AI Assistant is now fully operational offline.")
    else:
        print("ADB push failed. Please ensure USB debugging is enabled and your phone is connected.")

if __name__ == "__main__":
    main()
