import os

locales = {
    "es": "SwAIft",
    "fr": "SwAIft",
    "de": "SwAIft",
    "zh": "SwAIft",
    "zh-rTW": "SwAIft",
    "ja": "SwAIft",
    "ko": "SwAIft",
    "it": "SwAIft",
    "pt": "SwAIft",
    "ru": "SwAIft",
    "ar": "SwAIft",
    "hi": "SwAIft",
    "bn": "SwAIft",
    "pa": "SwAIft",
    "ur": "SwAIft",
    "fa": "SwAIft",
    "tr": "SwAIft",
    "vi": "SwAIft",
    "th": "SwAIft",
    "id": "SwAIft",
    "ms": "SwAIft",
    "tl": "SwAIft",
    "nl": "SwAIft",
    "pl": "SwAIft",
    "uk": "SwAIft",
    "ro": "SwAIft",
    "el": "SwAIft",
    "hu": "SwAIft",
    "cs": "SwAIft",
    "sv": "SwAIft",
    "da": "SwAIft",
    "fi": "SwAIft",
    "no": "SwAIft",
    "sk": "SwAIft",
    "bg": "SwAIft",
    "hr": "SwAIft",
    "sr": "SwAIft",
    "sl": "SwAIft",
    "lt": "SwAIft",
    "lv": "SwAIft",
    "et": "SwAIft",
    "he": "SwAIft",
    "sw": "SwAIft",
    "af": "SwAIft",
    "sq": "SwAIft",
    "am": "SwAIft",
    "hy": "SwAIft",
    "az": "SwAIft",
    "eu": "SwAIft",
    "be": "SwAIft",
    "bs": "SwAIft",
    "ca": "SwAIft",
    "ka": "SwAIft",
    "gu": "SwAIft",
    "is": "SwAIft",
    "kn": "SwAIft",
    "kk": "SwAIft",
    "km": "SwAIft",
    "lo": "SwAIft",
    "mk": "SwAIft",
    "ta": "SwAIft",
    "te": "SwAIft",
    "ml": "SwAIft",
    "mr": "SwAIft",
    "ne": "SwAIft"
}

for code, name in locales.items():
    dir_path = f"app/src/main/res/values-{code}"
    os.makedirs(dir_path, exist_ok=True)
    file_path = os.path.join(dir_path, "strings.xml")
    content = f'''<resources>
    <string name="app_name">{name}</string>
</resources>
'''
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

print(f"Successfully generated {len(locales)} localized strings.xml files.")
