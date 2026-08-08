import os
import glob

# Status translations mapping for res/values-*/strings.xml
translations = {
    "es": ("Estado: Activo y Monitoreando", "Estado: Desactivado"),
    "fr": ("Statut : Actif et en surveillance", "Statut : Désactivé"),
    "de": ("Status: Aktiv & Überwachung", "Status: Deaktiviert"),
    "it": ("Stato: Attivo e in monitoraggio", "Stato: Disattivato"),
    "pt": ("Status: Ativo e Monitorando", "Status: Desativado"),
    "ru": ("Статус: Активен и отслеживается", "Статус: Отключено"),
    "ja": ("ステータス：アクティブ＆監視中", "ステータス：無効"),
    "zh-rCN": ("状态：活跃与工作中", "状态：已禁用"),
    "zh-rTW": ("狀態：活躍與工作中", "狀態：已停用"),
    "ko": ("상태: 활성 및 모니터링 중", "상태: 비활성화됨"),
    "vi": ("Trạng thái: Hoạt động & Đang theo dõi", "Trạng thái: Đã tắt"),
    "hi": ("स्थिति: सक्रिय और निगरानी में", "स्थिति: निष्क्रिय"),
    "ar": ("الحالة: نشط وتحت المراقبة", "الحالة: معطل"),
    "id": ("Status: Aktif & Memantau", "Status: Nonaktif"),
    "in": ("Status: Aktif & Memantau", "Status: Nonaktif"),
    "th": ("สถานะ: ใช้งานและกำลังเฝ้าระวัง", "สถานะ: ปิดใช้งาน"),
    "tr": ("Durum: Aktif ve İzleniyor", "Durum: Devre Dışı"),
    "nl": ("Status: Actief & Monitoren", "Status: Uitgeschakeld"),
    "pl": ("Stan: Aktywny i monitorujący", "Stan: Wyłączony"),
    "sv": ("Status: Aktiv & Övervakar", "Status: Inaktiverad"),
    "da": ("Status: Aktiv & Overvåger", "Status: Deaktiveret"),
    "fi": ("Tila: Aktiivinen ja valvotaan", "Tila: Poistettu käytöstä"),
    "nb": ("Status: Aktiv og overvåker", "Status: Deaktivert"),
    "cs": ("Stav: Aktivní a monitoruje", "Stav: Zakázáno"),
    "uk": ("Статус: Активний та відстежується", "Статус: Вимкнено"),
    "hu": ("Státusz: Aktív és figyelő", "Státusz: Letiltva"),
    "ro": ("Stare: Activ și în monitorizare", "Stare: Dezactivat"),
    "el": ("Κατάσταση: Ενεργό & Παρακολούθηση", "Κατάσταση: Απενεργοποιημένο"),
    "bg": ("Статус: Активен и наблюдаващ", "Статус: Изключено"),
    "hr": ("Status: Aktivan i nadzire", "Status: Onemogućeno"),
    "sk": ("Stav: Aktívny a monitoruje", "Stav: Zakázané"),
    "sl": ("Stanje: Aktivno in spremlja", "Stanje: Onemogočeno"),
    "iw": ("סטטוס: פעיל ומנטר", "סטטוס: מבוטל"),
    "ms": ("Status: Aktif & Memantau", "Status: Dinyahaktifkan"),
    "fil": ("Katayuan: Aktibo at Subaybayan", "Katayuan: Naka-disable")
}

default_active = "Status: Active &amp; Monitoring"
default_disabled = "Status: Disabled"

res_dir = "app/src/main/res"
value_dirs = glob.glob(os.path.join(res_dir, "values-*"))

updated_count = 0
for vdir in sorted(value_dirs):
    strings_file = os.path.join(vdir, "strings.xml")
    if not os.path.exists(strings_file):
        continue

    dir_name = os.path.basename(vdir).replace("values-", "")
    active_txt, disabled_txt = translations.get(dir_name, ("Status: Active & Monitoring", "Status: Disabled"))
    
    # Escape ampersands for XML
    active_xml = active_txt.replace("&", "&amp;")
    disabled_xml = disabled_txt.replace("&", "&amp;")

    with open(strings_file, "r", encoding="utf-8") as f:
        content = f.read()

    # Remove existing status entries if present to prevent duplication
    lines = content.splitlines()
    filtered_lines = [l for l in lines if 'name="status_active_monitoring"' not in l and 'name="status_disabled"' not in l]
    new_content = "\n".join(filtered_lines)

    # Insert status strings before </resources>
    insert_str = f'    <string name="status_active_monitoring">{active_xml}</string>\n    <string name="status_disabled">{disabled_xml}</string>\n</resources>'
    new_content = new_content.replace("</resources>", insert_str)

    with open(strings_file, "w", encoding="utf-8") as f:
        f.write(new_content)
    updated_count += 1

print(f"✅ Successfully updated status translations across {updated_count} localization folders!")
