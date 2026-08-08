import os
import xml.etree.ElementTree as ET

# Base strings dictionary with key -> translations dict for curated languages
translations = {
    "app_name": "SwAIft",
    "activate": {
        "default": "Activate", "es": "Activar", "fr": "Activer", "de": "Aktivieren", "it": "Attiva",
        "pt": "Ativar", "ru": "Активировать", "ja": "有効化", "ko": "활성화", "zh-rCN": "激活",
        "zh-rTW": "啟動", "ar": "تفعيل", "hi": "सक्रिय करें", "bn": "সক্রিয় করুন", "tr": "Etkinleştir",
        "vi": "Kích hoạt", "th": "เปิดใช้งาน", "in": "Aktifkan", "nl": "Activeren", "pl": "Aktywuj",
        "sv": "Aktivera", "el": "Ενεργοποίηση", "fi": "Aktivoi", "da": "Aktiver", "nb": "Aktiver",
        "cs": "Aktivovat", "hu": "Aktiválás", "ro": "Activează", "uk": "Активувати", "bs": "Aktiviraj",
        "hr": "Aktiviraj", "sr": "Активирај", "sk": "Aktivovať", "bg": "Активирай", "he": "הפעל",
        "iw": "הפעל", "fa": "فعالسازی", "ur": "فعال کریں", "ms": "Aktifkan", "fil": "I-activate", "tl": "I-activate"
    },
    "pause": {
        "default": "Pause", "es": "Pausar", "fr": "Pauser", "de": "Pausieren", "it": "Metti in pausa",
        "pt": "Pausar", "ru": "Пауза", "ja": "一時停止", "ko": "일시중지", "zh-rCN": "暂停",
        "zh-rTW": "暫停", "ar": "إيقاف مؤقت", "hi": "रोकें", "bn": "বিরতি", "tr": "Duraklat",
        "vi": "Tạm dừng", "th": "พัก", "in": "Jeda", "nl": "Pauzeren", "pl": "Pauza",
        "sv": "Pausa", "el": "Παύση", "fi": "Tauko", "da": "Pause", "nb": "Pause",
        "cs": "Pozastavit", "hu": "Szünet", "ro": "Pauză", "uk": "Пауза", "bs": "Pauza",
        "hr": "Pauza", "sr": "Пауза", "sk": "Pozastaviť", "bg": "Пауза", "he": "השהה",
        "iw": "השהה", "fa": "مكث", "ur": "توقف", "ms": "Jeda", "fil": "I-pause", "tl": "I-pause"
    },
    "start": {
        "default": "Start", "es": "Iniciar", "fr": "Démarrer", "de": "Starten", "it": "Avvia",
        "pt": "Iniciar", "ru": "Старт", "ja": "開始", "ko": "시작", "zh-rCN": "开始",
        "zh-rTW": "開始", "ar": "بدء", "hi": "शुरू करें", "bn": "শুরু", "tr": "Başlat",
        "vi": "Bắt đầu", "th": "เริ่ม", "in": "Mulai", "nl": "Starten", "pl": "Start",
        "sv": "Starta", "el": "Έναρξη", "fi": "Aloita", "da": "Start", "nb": "Start",
        "cs": "Start", "hu": "Start", "ro": "Start", "uk": "Старт", "bs": "Pokreni",
        "hr": "Pokreni", "sr": "Покрени", "sk": "Štart", "bg": "Старт", "he": "התחל",
        "iw": "התחל", "fa": "شروع", "ur": "شروع کریں", "ms": "Mula", "fil": "Simulan", "tl": "Simulan"
    },
    "done": {
        "default": "Done", "es": "Hecho", "fr": "Terminé", "de": "Fertig", "it": "Fatto",
        "pt": "Concluído", "ru": "Готово", "ja": "完了", "ko": "완료", "zh-rCN": "完成",
        "zh-rTW": "完成", "ar": "تم", "hi": "हो गया", "bn": "সম্পন্ন", "tr": "Bitti",
        "vi": "Xong", "th": "เสร็จสิ้น", "in": "Selesai", "nl": "Klaar", "pl": "Gotowe",
        "sv": "Klar", "el": "Έτοιμο", "fi": "Valmis", "da": "Færdig", "nb": "Ferdig",
        "cs": "Hotovo", "hu": "Kész", "ro": "Gata", "uk": "Готово", "bs": "Gotovo",
        "hr": "Gotovo", "sr": "Готово", "sk": "Hotovo", "bg": "Готово", "he": "בוצע",
        "iw": "בוצע", "fa": "انجام شد", "ur": "مکمل", "ms": "Selesai", "fil": "Tapos na", "tl": "Tapos na"
    },
    "cancel": {
        "default": "Cancel", "es": "Cancelar", "fr": "Annuler", "de": "Abbrechen", "it": "Annulla",
        "pt": "Cancelar", "ru": "Отмена", "ja": "キャンセル", "ko": "취소", "zh-rCN": "取消",
        "zh-rTW": "取消", "ar": "إلغاء", "hi": "रद्द करें", "bn": "বাতিল", "tr": "İptal",
        "vi": "Hủy", "th": "ยกเลิก", "in": "Batal", "nl": "Annuleren", "pl": "Anuluj",
        "sv": "Avbryt", "el": "Aκύρωση", "fi": "Peruuta", "da": "Annuller", "nb": "Avbryt",
        "cs": "Zrušit", "hu": "Mégse", "ro": "Anulează", "uk": "Скасувати", "bs": "Otkaži",
        "hr": "Otkaži", "sr": "Откажи", "sk": "Zrušiť", "bg": "Отказ", "he": "ביטול",
        "iw": "ביטול", "fa": "لغو", "ur": "منسوخ", "ms": "Batal", "fil": "Kanselahin", "tl": "Kanselahin"
    },
    "save": {
        "default": "Save", "es": "Guardar", "fr": "Enregistrer", "de": "Speichern", "it": "Salva",
        "pt": "Salvar", "ru": "Сохранить", "ja": "保存", "ko": "저장", "zh-rCN": "保存",
        "zh-rTW": "儲存", "ar": "حفظ", "hi": "सहेजें", "bn": "সংরক্ষণ", "tr": "Kaydet",
        "vi": "Lưu", "th": "บันทึก", "in": "Simpan", "nl": "Opslaan", "pl": "Zapisz",
        "sv": "Spara", "el": "Αποθήκευση", "fi": "Tallenna", "da": "Gem", "nb": "Lagre",
        "cs": "Uložit", "hu": "Mentés", "ro": "Salvează", "uk": "Зберегти", "bs": "Sačuvaj",
        "hr": "Spremi", "sr": "Сачувај", "sk": "Uložiť", "bg": "Запази", "he": "שמור",
        "iw": "שמור", "fa": "ذخیره", "ur": "محفوظ کریں", "ms": "Simpan", "fil": "I-save", "tl": "I-save"
    },
    "delete": {
        "default": "Delete", "es": "Eliminar", "fr": "Supprimer", "de": "Löschen", "it": "Elimina",
        "pt": "Excluir", "ru": "Удалить", "ja": "削除", "ko": "削除", "zh-rCN": "删除",
        "zh-rTW": "刪除", "ar": "حذف", "hi": "हटाएं", "bn": "মুছুন", "tr": "Sil",
        "vi": "Xóa", "th": "ลบ", "in": "Hapus", "nl": "Verwijderen", "pl": "Usuń",
        "sv": "Ta bort", "el": "Διαγραφή", "fi": "Poista", "da": "Slet", "nb": "Slett",
        "cs": "Smazat", "hu": "Törlés", "ro": "Șterge", "uk": "Видалити", "bs": "Izbriši",
        "hr": "Izbriši", "sr": "Избриши", "sk": "Vymazať", "bg": "Изтрий", "he": "מחק",
        "iw": "מחק", "fa": "حذف", "ur": "حذف کریں", "ms": "Padam", "fil": "Burahin", "tl": "Burahin"
    },
    "edit": {
        "default": "Edit", "es": "Editar", "fr": "Modifier", "de": "Bearbeiten", "it": "Modifica",
        "pt": "Editar", "ru": "Редактировать", "ja": "編集", "ko": "編集", "zh-rCN": "编辑",
        "zh-rTW": "編輯", "ar": "تعديل", "hi": "संपादित करें", "bn": "সম্পাদনা", "tr": "Düzenle",
        "vi": "Sửa", "th": "แก้ไข", "in": "Edit", "nl": "Bewerken", "pl": "Edytuj",
        "sv": "Redigera", "el": "Επεξεργασία", "fi": "Muokkaa", "da": "Rediger", "nb": "Rediger",
        "cs": "Upravit", "hu": "Szerkesztés", "ro": "Editează", "uk": "Редагувати", "bs": "Uredi",
        "hr": "Uredi", "sr": "Уреди", "sk": "Upraviť", "bg": "Редактирай", "he": "ערוך",
        "iw": "ערוך", "fa": "ویرایش", "ur": "ترمیم", "ms": "Sunting", "fil": "Baguhin", "tl": "Baguhin"
    },
    "create_routine": {
        "default": "Create Routine", "es": "Crear Rutina", "fr": "Créer une routine", "de": "Routine erstellen",
        "it": "Crea Routine", "pt": "Criar Rotina", "ru": "Создать сценарий", "ja": "ルーティン作成",
        "ko": "루틴 만들기", "zh-rCN": "创建流程", "zh-rTW": "建立流程", "ar": "إنشاء روتين",
        "hi": "रूटिन बनाएं", "bn": "রুটিন তৈরি করুন", "tr": "Rutin Oluştur", "vi": "Tạo quy trình",
        "th": "สร้างกิจวัตร", "in": "Buat Rutinitas", "nl": "Routine maken", "pl": "Utwórz rutynę",
        "sv": "Skapa rutin", "bs": "Kreiraj rutinu", "hr": "Stvori rutinu", "uk": "Створити рутину"
    },
    "nav_dashboard": {
        "default": "Dashboard", "es": "Panel", "fr": "Tableau de bord", "de": "Dashboard", "it": "Dashboard",
        "pt": "Painel", "ru": "Дашборд", "ja": "ダッシュボード", "ko": "대시보드", "zh-rCN": "仪表板",
        "zh-rTW": "儀表板", "ar": "لوحة التحكم", "hi": "डैशबोर्ड", "bn": "ড্যাশবোর্ড", "tr": "Kontrol Paneli",
        "vi": "Bảng điều khiển", "th": "แผงควบคุม", "in": "Dasbor", "nl": "Dashboard", "pl": "Pulpit",
        "sv": "Översikt", "bs": "Nadzorna ploča", "hr": "Nadzorna ploča", "uk": "Панель"
    },
    "nav_my_flows": {
        "default": "My Flows", "es": "Mis Flujos", "fr": "Mes Flux", "de": "Meine Abläufe", "it": "I Miei Flussi",
        "pt": "Meus Fluxos", "ru": "Мои Сценарии", "ja": "マイフロー", "ko": "내 플로우", "zh-rCN": "我的流程",
        "zh-rTW": "我的流程", "ar": "مساراتي", "hi": "मेरे फ्लो", "bn": "আমার ফ্লো", "tr": "Akışlarım",
        "vi": "Quy trình của tôi", "th": "ฟลอว์ของฉัน", "in": "Alur Saya", "nl": "Mijn Flows", "pl": "Moje przepływy",
        "sv": "Mina flöden", "bs": "Moji tokovi", "hr": "Moji tokovi", "uk": "Мої потоки"
    },
    "nav_discover": {
        "default": "Discover", "es": "Descubrir", "fr": "Découvrir", "de": "Entdecken", "it": "Esplora",
        "pt": "Descobrir", "ru": "Обзор", "ja": "見つける", "ko": "탐색", "zh-rCN": "发现",
        "zh-rTW": "探索", "ar": "استكشاف", "hi": "खोजें", "bn": "আবিষ্কার", "tr": "Keşfet",
        "vi": "Khám phá", "th": "ค้นพบ", "in": "Jelajahi", "nl": "Ontdekken", "pl": "Odkrywaj",
        "sv": "Utforska", "bs": "Otkrij", "hr": "Otkrij", "uk": "Огляд"
    },
    "nav_history": {
        "default": "History", "es": "Historial", "fr": "Historique", "de": "Verlauf", "it": "Cronologia",
        "pt": "Histórico", "ru": "История", "ja": "履歴", "ko": "기록", "zh-rCN": "历史记录",
        "zh-rTW": "歷史記錄", "ar": "السجل", "hi": "इतिहास", "bn": "ইতিহাস", "tr": "Geçmiş",
        "vi": "Lịch sử", "th": "ประวัติ", "in": "Riwayat", "nl": "Geschiedenis", "pl": "Historia",
        "sv": "Historik", "bs": "Historija", "hr": "Povijest", "uk": "Історія"
    },
    "nav_settings": {
        "default": "Settings", "es": "Ajustes", "fr": "Paramètres", "de": "Einstellungen", "it": "Impostazioni",
        "pt": "Configurações", "ru": "Настройки", "ja": "設定", "ko": "설정", "zh-rCN": "设置",
        "zh-rTW": "設定", "ar": "الإعدادات", "hi": "सेटिंग्स", "bn": "সেটিংস", "tr": "Ayarlar",
        "vi": "Cài đặt", "th": "การตั้งค่า", "in": "Pengaturan", "nl": "Instellingen", "pl": "Ustawienia",
        "sv": "Inställningar", "bs": "Postavke", "hr": "Postavke", "uk": "Налаштування"
    },
    "env_conditions_title": {
        "default": "Environment Conditions", "es": "Condiciones Ambientales", "fr": "Conditions Environnementales", "de": "Umgebungsbedingungen",
        "it": "Condizioni Ambientali", "pt": "Condições Ambientais", "ru": "Условия Окружения", "ja": "環境条件",
        "ko": "환경 조건", "zh-rCN": "环境条件", "zh-rTW": "環境條件", "ar": "الظروف البيئية",
        "hi": "पर्यावरण स्थितियां", "bs": "Okolišni uslovi", "hr": "Uvjeti okruženja", "uk": "Умови середовища"
    },
    "env_gps_location": {
        "default": "GPS Location Geofence", "es": "Ubicación GPS", "fr": "Localisation GPS", "de": "GPS-Standort",
        "it": "Posizione GPS", "pt": "Localização GPS", "ru": "GPS Локация", "ja": "GPS位置情報",
        "ko": "GPS 위치", "zh-rCN": "GPS地理位置", "zh-rTW": "GPS地理位置", "ar": "موقع GPS",
        "hi": "जीपीएस स्थान", "bs": "GPS lokacija", "hr": "GPS lokacija", "uk": "GPS географічне положення"
    },
    "env_wifi_ssid": {
        "default": "Wi-Fi SSID Connection", "es": "Conexión Wi-Fi", "fr": "Connexion Wi-Fi", "de": "WLAN-Verbindung",
        "it": "Connessione Wi-Fi", "pt": "Conexão Wi-Fi", "ru": "Wi-Fi Подключение", "ja": "Wi-Fi接続",
        "ko": "Wi-Fi 연결", "zh-rCN": "Wi-Fi网络连接", "zh-rTW": "Wi-Fi網路連線", "ar": "اتصال شبكة Wi-Fi",
        "hi": "वाई-फाई कनेक्शन", "bs": "Wi-Fi konekcija", "hr": "Wi-Fi veza", "uk": "Wi-Fi з'єднання"
    },
    "env_charger_plugged": {
        "default": "Charger Plugged in", "es": "Cargador Conectado", "fr": "Chargeur Connecté", "de": "Ladegerät angeschlossen",
        "it": "Caricabatterie Collegato", "pt": "Carregador Conectado", "ru": "Зарядное Устройство Подключено", "ja": "充電器接続中",
        "ko": "충전기 연결됨", "zh-rCN": "已插入充电器", "zh-rTW": "已插入充電器", "ar": "الشاحن متصل",
        "hi": "चार्जर कनेक्टेड", "bs": "Punjač priključen", "hr": "Punjač priključen", "uk": "Зарядний пристрій підключено"
    },
    "env_open_app_context": {
        "default": "Open App Context", "es": "Contexto de Aplicación", "fr": "Contexte d'Application", "de": "App-Kontext",
        "it": "Contesto App", "pt": "Contexto de App", "ru": "Контекст Приложения", "ja": "アプリコンテキスト",
        "ko": "앱 컨텍스트", "zh-rCN": "应用场景Context", "zh-rTW": "應用程式情境", "ar": "سياق التطبيق",
        "hi": "ऐप संदर्भ", "bs": "Kontekst aplikacije", "hr": "Kontekst aplikacije", "uk": "Контекст додатка"
    },
    "env_activity_motion": {
        "default": "Activity Motion", "es": "Actividad Física", "fr": "Activité Physique", "de": "Aktivität",
        "it": "Attività e Movimento", "pt": "Atividade Física", "ru": "Физическая Активность", "ja": "運動アクティビティ",
        "ko": "활동 상태", "zh-rCN": "运动状态", "zh-rTW": "運動狀態", "ar": "نشاط الحركة",
        "hi": "गतिविधि गति", "bs": "Kretanje i aktivnost", "hr": "Aktivnost kretanja", "uk": "Активність руху"
    },
    "env_time_schedule": {
        "default": "Time of Day Schedule", "es": "Horario", "fr": "Horaire", "de": "Zeitplan",
        "it": "Orario", "pt": "Horário", "ru": "Расписание Времени", "ja": "時間スケジュール",
        "ko": "시간 일정", "zh-rCN": "时间安排", "zh-rTW": "時間安排", "ar": "جدول الوقت",
        "hi": "समय अनुसूची", "bs": "Vremenski raspored", "hr": "Vremenski raspored", "uk": "Розклад часу"
    },
    "status_none": {
        "default": "None", "es": "Ninguno", "fr": "Aucun", "de": "Keine", "it": "Nessuno",
        "pt": "Nenhum", "ru": "Нет", "ja": "なし", "ko": "없음", "zh-rCN": "无",
        "zh-rTW": "無", "ar": "لا يوجد", "hi": "कोई नहीं", "bs": "Nijedan", "hr": "Nijedan", "uk": "Немає"
    },
    "status_disconnected": {
        "default": "Disconnected", "es": "Desconectado", "fr": "Déconnecté", "de": "Getrennt", "it": "Disconnesso",
        "pt": "Desconectado", "ru": "Отключено", "ja": "未接続", "ko": "연결 해제됨", "zh-rCN": "未连接",
        "zh-rTW": "未連線", "ar": "غير متصل", "hi": "डिस्कनेक्ट किया गया", "bs": "Offine", "hr": "Odspojeno", "uk": "Відключено"
    },
    "status_active": {
        "default": "ACTIVE", "es": "ACTIVO", "fr": "ACTIF", "de": "AKTIV", "it": "ATTIVO",
        "pt": "ATIVO", "ru": "АКТИВЕН", "ja": "アクティブ", "ko": "활성", "zh-rCN": "运行中",
        "zh-rTW": "運行中", "ar": "نشط", "hi": "सक्रिय", "bs": "AKTIVNO", "hr": "AKTIVNO", "uk": "АКТИВНО"
    },
    "status_paused": {
        "default": "PAUSED", "es": "PAUSADO", "fr": "EN PAUSE", "de": "PAUSIERT", "it": "IN PAUSA",
        "pt": "PAUSADO", "ru": "ПАУЗА", "ja": "一時停止中", "ko": "일시중지됨", "zh-rCN": "已暂停",
        "zh-rTW": "已暫停", "ar": "متوقف مؤقتاً", "hi": "रुका हुआ", "bs": "PAUZIRANO", "hr": "PAUZIRANO", "uk": "ПАУЗА"
    },
    "category_all": {
        "default": "All", "es": "Todos", "fr": "Tous", "de": "Alle", "it": "Tutti",
        "pt": "Todos", "ru": "Все", "ja": "すべて", "ko": "전체", "zh-rCN": "全部",
        "zh-rTW": "全部", "ar": "الكل", "hi": "सभी", "bs": "Sve", "hr": "Sve", "uk": "Усі"
    },
    "category_battery": {
        "default": "Battery", "es": "Batería", "fr": "Batterie", "de": "Akku", "it": "Batteria",
        "pt": "Bateria", "ru": "Батарея", "ja": "バッテリー", "ko": "배터리", "zh-rCN": "电池",
        "zh-rTW": "電池", "ar": "البطارية", "hi": "बैटरी", "bs": "Baterija", "hr": "Baterija", "uk": "Батарея"
    },
    "category_automation": {
        "default": "Automation", "es": "Automatización", "fr": "Automatisation", "de": "Automatisierung", "it": "Automazione",
        "pt": "Automação", "ru": "Автоматизация", "ja": "自動化", "ko": "자동화", "zh-rCN": "自动化",
        "zh-rTW": "自動化", "ar": "الأتمتة", "hi": "ऑटोमेशन", "bs": "Automatizacija", "hr": "Automatizacija", "uk": "Автоматизація"
    },
    "category_smart_home": {
        "default": "Smart Home", "es": "Hogar Inteligente", "fr": "Maison Connectée", "de": "Smart Home", "it": "Casa Intelligente",
        "pt": "Casa Inteligente", "ru": "Умный Дом", "ja": "スマートホーム", "ko": "스마트홈", "zh-rCN": "智能家居",
        "zh-rTW": "智慧家居", "ar": "المنزل الذكي", "hi": "स्मार्ट होम", "bs": "Pametna kuća", "hr": "Pametan dom", "uk": "Розумний дім"
    },
    "category_productivity": {
        "default": "Productivity", "es": "Productividad", "fr": "Productivité", "de": "Produktivität", "it": "Produttività",
        "pt": "Produtividade", "ru": "Продуктивность", "ja": "生産性", "ko": "생산성", "zh-rCN": "工作效率",
        "zh-rTW": "工作效率", "ar": "الإنتاجية", "hi": "उत्पादकता", "bs": "Produktivnost", "hr": "Produktivnost", "uk": "Продуктивність"
    },
    "category_health": {
        "default": "Health", "es": "Salud", "fr": "Santé", "de": "Gesundheit", "it": "Salute",
        "pt": "Saúde", "ru": "Здоровье", "ja": "ヘルスケア", "ko": "건강", "zh-rCN": "健康",
        "zh-rTW": "健康", "ar": "الصحة", "hi": "स्वास्थ्य", "bs": "Zdravlje", "hr": "Zdravlje", "uk": "Здоров'я"
    },
    # Smart Rate & Share prompts
    "rate_title": {
        "default": "Enjoying SwAIft?", "es": "¿Te gusta SwAIft?", "fr": "Vous aimez SwAIft?", "de": "Gefällt Dir SwAIft?",
        "it": "Ti piace SwAIft?", "pt": "Gostando do SwAIft?", "ru": "Нравится SwAIft?", "ja": "SwAIftをお楽しみですか？",
        "ko": "SwAIft가 마음에 드시나요?", "zh-rCN": "喜欢使用 SwAIft 吗？", "zh-rTW": "喜歡使用 SwAIft 嗎？", "ar": "هل تستمتع بـ SwAIft؟",
        "hi": "SwAIft पसंद आ रहा है?", "bs": "Uživate u SwAIft-u?", "hr": "Uživate u SwAIft-u?", "uk": "Подобається SwAIft?"
    },
    "rate_description": {
        "default": "If SwAIft helps automate your day, please rate us 5 stars on Play Store!",
        "es": "Si SwAIft te ayuda a automatizar tu día, ¡valóranos con 5 estrellas en Play Store!",
        "fr": "Si SwAIft simplifie votre journée, attribuez-nous 5 étoiles sur le Play Store!",
        "de": "Wenn SwAIft Deinen Alltag erleichtert, bewerten Sie uns bitte mit 5 Sternen im Play Store!",
        "ja": "SwAIftが日常の自動化に役立っている場合は、Play Storeで5つ星評価をお願いします！",
        "zh-rCN": "如果 SwAIft 帮到了您的日常生活，请在 Play 商店给予我们5星好评！",
        "zh-rTW": "如果 SwAIft 幫到了您的日常生活，請在 Play 商店給予我們5星好評！",
        "bs": "Ako vam SwAIft pomaže u automatizaciji, ocijenite nas sa 5 zvjezdica na Play Store-u!"
    },
    "rate_btn": {
        "default": "Rate 5 Stars ⭐", "es": "Calificar 5 Estrellas ⭐", "fr": "Noter 5 Étoiles ⭐", "de": "5 Sterne bewerten ⭐",
        "it": "Valuta 5 Stelle ⭐", "pt": "Avaliar 5 Estrelas ⭐", "ru": "Оценить на 5 ⭐", "ja": "5つ星で評価 ⭐",
        "ko": "별 5개 평가 ⭐", "zh-rCN": "五星好评 ⭐", "zh-rTW": "五星好評 ⭐", "ar": "تقييم 5 نجوم ⭐",
        "hi": "5 स्टार रेटिंग ⭐", "bs": "Ocijeni 5 zvjezdica ⭐", "hr": "Ocijeni 5 zvjezdica ⭐", "uk": "Оцінити на 5 ⭐"
    },
    "share_title": {
        "default": "Share SwAIft with Friends", "es": "Compartir SwAIft con amigos", "fr": "Partager SwAIft avec vos amis",
        "de": "SwAIft mit Freunden teilen", "it": "Condividi SwAIft con gli amici", "pt": "Compartilhar SwAIft com amigos",
        "ru": "Поделиться SwAIft с друзьями", "ja": "友達にSwAIftを共有する", "ko": "친구에게 SwAIft 공유하기",
        "zh-rCN": "与好友分享 SwAIft", "zh-rTW": "與好友分享 SwAIft", "ar": "مشاركة SwAIft مع الأصدقاء",
        "hi": "दोस्तों के साथ SwAIft शेयर करें", "bs": "Podijeli SwAIft s prijateljima", "hr": "Podijeli SwAIft s prijateljima", "uk": "Поділитися SwAIft з друзями"
    },
    "share_description": {
        "default": "Help others discover smart, private Android automation!",
        "es": "¡Ayuda a otros a descubrir la automatización inteligente e idónea para Android!",
        "fr": "Aidez d'autres personnes à découvrir l'automatisation Android privée et intelligente!",
        "de": "Hilf anderen, smarte und private Android-Automation zu entdecken!",
        "ja": "スマートでプライバシーに配慮したAndroid自動化を友達にも教えてあげましょう！",
        "zh-rCN": "帮助更多人体验智能且注重隐私的 Android 自动化！",
        "zh-rTW": "幫助更多人體驗智慧且注重隱私的 Android 自動化！",
        "bs": "Pomozite drugima da otkriju pametnu i privatnu Android automatizaciju!"
    },
    "share_btn": {
        "default": "Share App 🚀", "es": "Compartir App 🚀", "fr": "Partager l'App 🚀", "de": "App teilen 🚀",
        "it": "Condividi App 🚀", "pt": "Compartilhar App 🚀", "ru": "Поделиться 🚀", "ja": "アプリを共有 🚀",
        "ko": "앱 공유하기 🚀", "zh-rCN": "分享应用 🚀", "zh-rTW": "分享應用程式 🚀", "ar": "مشاركة التطبيق 🚀",
        "hi": "ऐप शेयर करें 🚀", "bs": "Podijeli aplikaciju 🚀", "hr": "Podijeli aplikaciju 🚀", "uk": "Поділитися 🚀"
    },
    "maybe_later": {
        "default": "Maybe Later", "es": "Quizás más tarde", "fr": "Plus tard", "de": "Vielleicht später",
        "it": "Forse più tardi", "pt": "Talvez mais tarde", "ru": "Позже", "ja": "後で",
        "ko": "나중에", "zh-rCN": "以后再说", "zh-rTW": "以後再說", "ar": "ربما لاحقاً",
        "hi": "शायद बाद में", "bs": "Možda kasnije", "hr": "Možda kasnije", "uk": "Пізніше"
    }
}

langs = [
  "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs",
  "da", "de", "el", "es", "et", "eu", "fa", "fi", "fil", "fr",
  "gl", "gu", "hi", "hr", "hu", "hy", "in", "is", "it", "iw",
  "ja", "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv",
  "mk", "ml", "mn", "mr", "ms", "my", "nb", "ne", "nl", "pa",
  "pl", "pt", "ro", "ru", "si", "sk", "sl", "sq", "sr", "sv",
  "sw", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "zh-rCN", "zh-rTW"
]

import xml.etree.ElementTree as ET

base_xml_path = "app/src/main/res/values/strings.xml"
tree = ET.parse(base_xml_path)
root = tree.getroot()

base_keys = {}
for elem in root.findall('string'):
    name = elem.attrib.get('name')
    text = elem.text or ''
    if name:
        base_keys[name] = text

base_dir = "app/src/main/res"

count = 0
for lang in langs:
    target_dir = os.path.join(base_dir, f"values-{lang}")
    os.makedirs(target_dir, exist_ok=True)
    xml_path = os.path.join(target_dir, "strings.xml")
    
    lines = ["<resources>\n"]
    for key, default_text in base_keys.items():
        val_dict = translations.get(key)
        if isinstance(val_dict, str):
            text = val_dict
        elif isinstance(val_dict, dict):
            text = val_dict.get(lang, val_dict.get("default", default_text))
        else:
            text = default_text
        text_escaped = text.replace("&", "&amp;").replace("'", "\\'")
        lines.append(f'    <string name="{key}">{text_escaped}</string>\n')
    lines.append("</resources>\n")

    with open(xml_path, "w", encoding="utf-8") as f:
        f.writelines(lines)
    count += 1

print(f"✅ Generated full strings.xml for all {count} localization directories with {len(base_keys)} keys each!")
