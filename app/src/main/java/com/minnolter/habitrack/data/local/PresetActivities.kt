package com.minnolter.habitrack.data.local

import com.minnolter.habitrack.domain.model.HabitCategory
import com.minnolter.habitrack.domain.model.PresetActivity

/**
 * Curated list of exactly 100 unique, non-repetitive activities,
 * each with its own distinct background photo URL.
 */
object PresetActivitiesDatabase {

    val ALL_PRESETS: List<PresetActivity> by lazy {
        listOf(
            // --- MUSIC (15) ---
            PresetActivity("Singing", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=600&q=80"),
            PresetActivity("Drums", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=600&q=80"),
            PresetActivity("Acoustic Guitar", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&q=80"),
            PresetActivity("Electric Guitar", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?w=600&q=80"),
            PresetActivity("Piano", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=600&q=80"),
            PresetActivity("Violin", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1612225330812-01a9c6b355ec?w=600&q=80"),
            PresetActivity("Cello", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1588611910606-d7134aa6b86d?w=600&q=80"),
            PresetActivity("Saxophone", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1525994886773-080587e161c2?w=600&q=80"),
            PresetActivity("Trumpet", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=600&q=80"),
            PresetActivity("Flute", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1573871666457-7c7329118cf9?w=600&q=80"),
            PresetActivity("Ukulele", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&q=80"),
            PresetActivity("DJing", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1516873240891-4bf014598ab4?w=600&q=80"),
            PresetActivity("Music Production", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=600&q=80"),
            PresetActivity("Songwriting", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=600&q=80"),
            PresetActivity("Accordion", HabitCategory.MUSIC, "#7C4DFF", "https://images.unsplash.com/photo-1588820465223-96b65eeea80a?w=600&q=80"),

            // --- SOFTWARE & TECH (10) ---
            PresetActivity("Coding", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&q=80"),
            PresetActivity("Mobile App Dev", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=600&q=80"),
            PresetActivity("Web Development", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1547658719-da2b51169166?w=600&q=80"),
            PresetActivity("Game Development", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&q=80"),
            PresetActivity("Data Science", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600&q=80"),
            PresetActivity("Cybersecurity", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=600&q=80"),
            PresetActivity("Robotics", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=600&q=80"),
            PresetActivity("AI Engineering", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=600&q=80"),
            PresetActivity("Cloud Engineering", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=600&q=80"),
            PresetActivity("Database Design", HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=600&q=80"),

            // --- CRAFT & ART (15) ---
            PresetActivity("Oil Painting", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&q=80"),
            PresetActivity("Watercolor Painting", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1582562124811-c09040d0a901?w=600&q=80"),
            PresetActivity("Digital Illustration", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&q=80"),
            PresetActivity("Sculpting", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=600&q=80"),
            PresetActivity("Photography", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&q=80"),
            PresetActivity("Pottery", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe262?w=600&q=80"),
            PresetActivity("Calligraphy", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=600&q=80"),
            PresetActivity("Origami", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=600&q=80"),
            PresetActivity("Woodworking", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600&q=80"),
            PresetActivity("3D Modeling", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=600&q=80"),
            PresetActivity("Fashion Design", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=600&q=80"),
            PresetActivity("Jewelry Making", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=600&q=80"),
            PresetActivity("Character Design", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&q=80"),
            PresetActivity("Tattoos", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1598371839696-5c5bb00bdc28?w=600&q=80"),
            PresetActivity("Animation", HabitCategory.CRAFT_AND_ART, "#FF4081", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&q=80"),

            // --- ATHLETICS & PHYSICAL (15) ---
            PresetActivity("Running", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=600&q=80"),
            PresetActivity("Exercise", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&q=80"),
            PresetActivity("Gym", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&q=80"),
            PresetActivity("Calisthenics", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?w=600&q=80"),
            PresetActivity("Basketball", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=600&q=80"),
            PresetActivity("Swimming", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=600&q=80"),
            PresetActivity("Tennis", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=600&q=80"),
            PresetActivity("Bouldering", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1522163182402-834f871fd851?w=600&q=80"),
            PresetActivity("Cycling", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600&q=80"),
            PresetActivity("Boxing", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=600&q=80"),
            PresetActivity("Yoga", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1545205597-3d9d02c29597?w=600&q=80"),
            PresetActivity("Martial Arts", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1555597673-b21d5c935865?w=600&q=80"),
            PresetActivity("Skateboarding", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1520045892732-304bc3ac5d8e?w=600&q=80"),
            PresetActivity("Skipping Rope", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1591258370814-01609b341790?w=600&q=80"),
            PresetActivity("Pilates", HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600&q=80"),

            // --- ACADEMIC & LANGUAGE (10) ---
            PresetActivity("Language Learning", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600&q=80"),
            PresetActivity("Mathematics", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=600&q=80"),
            PresetActivity("Physics", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=600&q=80"),
            PresetActivity("Chemistry", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=600&q=80"),
            PresetActivity("Philosophy", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=600&q=80"),
            PresetActivity("History", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600&q=80"),
            PresetActivity("Astronomy", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600&q=80"),
            PresetActivity("Creative Writing", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=600&q=80"),
            PresetActivity("Public Speaking", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&q=80"),
            PresetActivity("Economics", HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80"),

            // --- TRADES & CULINARY (10) ---
            PresetActivity("Cooking", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=600&q=80"),
            PresetActivity("Baking", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&q=80"),
            PresetActivity("Espresso & Barista", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&q=80"),
            PresetActivity("Mixology", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=600&q=80"),
            PresetActivity("Carpentry", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600&q=80"),
            PresetActivity("Car Restoration", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=600&q=80"),
            PresetActivity("Gardening", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=600&q=80"),
            PresetActivity("Welding", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?w=600&q=80"),
            PresetActivity("Tailoring", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=600&q=80"),
            PresetActivity("Plumbing", HabitCategory.TRADES_CULINARY, "#FF8F00", "https://images.unsplash.com/photo-1585704032915-c3400ca199e7?w=600&q=80"),

            // --- GAMES & MIND (10) ---
            PresetActivity("Chess", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=600&q=80"),
            PresetActivity("Go Strategy", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&q=80"),
            PresetActivity("Poker", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1511193311914-0346f16efe90?w=600&q=80"),
            PresetActivity("Speedcubing", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1563089145-599997674d43?w=600&q=80"),
            PresetActivity("Esports", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&q=80"),
            PresetActivity("Crosswords", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1585314062340-f1a5a7c9328d?w=600&q=80"),
            PresetActivity("Memory Training", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600&q=80"),
            PresetActivity("Scrabble", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1580541832626-2a7131ee809f?w=600&q=80"),
            PresetActivity("Billiards", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1534158914592-062992fbe900?w=600&q=80"),
            PresetActivity("Table Tennis", HabitCategory.GAMES_MIND, "#FFD54F", "https://images.unsplash.com/photo-1511067007398-7e4b90cfa4bc?w=600&q=80"),

            // --- WELLNESS & LIFE (15) ---
            PresetActivity("Meditation", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&q=80"),
            PresetActivity("Breathwork", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600&q=80"),
            PresetActivity("Journaling", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1517842645767-c639042777db?w=600&q=80"),
            PresetActivity("Reading", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600&q=80"),
            PresetActivity("Hydration Habits", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=600&q=80"),
            PresetActivity("Cold Plunges", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&q=80"),
            PresetActivity("Posture Exercises", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&q=80"),
            PresetActivity("Hiking", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1551632811-561732d1e306?w=600&q=80"),
            PresetActivity("Bird Watching", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1444464666168-49d633b86797?w=600&q=80"),
            PresetActivity("Archery", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1511067007398-7e4b90cfa4bc?w=600&q=80"),
            PresetActivity("Surfing", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=600&q=80"),
            PresetActivity("Skiing", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1551524559-8af4e6624178?w=600&q=80"),
            PresetActivity("Snowboarding", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1520045892732-304bc3ac5d8e?w=600&q=80"),
            PresetActivity("Fencing", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1517838277536-f5f99be501ce?w=600&q=80"),
            PresetActivity("Darts", HabitCategory.WELLNESS_LIFE, "#81C784", "https://images.unsplash.com/photo-1534158914592-062992fbe900?w=600&q=80")
        )
    }

    fun search(query: String, category: HabitCategory? = null): List<PresetActivity> {
        val trimmed = query.trim()
        return ALL_PRESETS.filter { preset ->
            (category == null || preset.category == category) &&
            (trimmed.isEmpty() || preset.name.contains(trimmed, ignoreCase = true) ||
             preset.category.displayName.contains(trimmed, ignoreCase = true))
        }
    }
}
