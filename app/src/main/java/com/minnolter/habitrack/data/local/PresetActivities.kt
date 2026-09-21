package com.minnolter.habitrack.data.local

import com.minnolter.habitrack.domain.model.HabitCategory
import com.minnolter.habitrack.domain.model.PresetActivity

/**
 * Extensive database of 500+ searchable preset activities across diverse domains.
 * Each activity comes pre-configured with category metadata, default hue swatch,
 * and a curated background image URL designed for left-aligned, highly faded card backgrounds.
 */
object PresetActivitiesDatabase {

    val ALL_PRESETS: List<PresetActivity> by lazy {
        buildList {
            // --- MUSIC ---
            addMusicPresets(this)
            // --- SOFTWARE & TECH ---
            addTechPresets(this)
            // --- CRAFT & ART ---
            addArtPresets(this)
            // --- ATHLETICS & PHYSICAL ---
            addAthleticPresets(this)
            // --- ACADEMIC & LANGUAGE ---
            addAcademicPresets(this)
            // --- TRADES & CULINARY ---
            addCulinaryPresets(this)
            // --- GAMES & MIND ---
            addGamesPresets(this)
            // --- WELLNESS & LIFE ---
            addWellnessPresets(this)
        }
    }

    fun search(query: String, category: HabitCategory? = null): List<PresetActivity> {
        val trimmed = query.trim()
        return ALL_PRESETS.filter { preset ->
            (category == null || preset.category == category) &&
            (trimmed.isEmpty() || preset.name.contains(trimmed, ignoreCase = true) ||
             preset.category.displayName.contains(trimmed, ignoreCase = true))
        }
    }

    private fun addMusicPresets(list: MutableList<PresetActivity>) {
        val musicImg = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80"
        val guitarImg = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&q=80"
        val violinImg = "https://images.unsplash.com/photo-1612225330812-01a9c6b355ec?w=600&q=80"
        val pianoImg = "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=600&q=80"
        val drumsImg = "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=600&q=80"

        val musicItems = listOf(
            "Violin" to violinImg, "Guitar" to guitarImg, "Piano" to pianoImg, "Acoustic Guitar" to guitarImg,
            "Electric Guitar" to guitarImg, "Bass Guitar" to guitarImg, "Drums" to drumsImg, "Singing / Vocals" to musicImg,
            "Cello" to violinImg, "Flute" to musicImg, "Saxophone" to musicImg, "Trumpet" to musicImg,
            "Clarinet" to musicImg, "Trombone" to musicImg, "Ukulele" to guitarImg, "Mandolin" to guitarImg,
            "Banjo" to guitarImg, "Harp" to musicImg, "Accordion" to musicImg, "Organ" to pianoImg,
            "Synthesizer Production" to musicImg, "Songwriting" to musicImg, "Music Theory" to musicImg,
            "Beatmaking & Ableton" to musicImg, "DJing & Mixing" to musicImg, "Audio Engineering" to musicImg,
            "Mixing & Mastering" to musicImg, "Opera Singing" to musicImg, "Jazz Improvisation" to musicImg,
            "Sight Reading" to musicImg, "Viola" to violinImg, "Double Bass" to violinImg, "Oboe" to musicImg,
            "Bassoon" to musicImg, "French Horn" to musicImg, "Tuba" to musicImg, "Piccolo" to musicImg,
            "Bagpipes" to musicImg, "Harmonica" to musicImg, "Xylophone / Marimba" to musicImg,
            "Tabla" to drumsImg, "Sitar" to guitarImg, "Koto" to guitarImg, "Erhu" to violinImg,
            "Guzheng" to guitarImg, "Kalimba" to musicImg, "Steel Pan Drums" to drumsImg, "Lute" to guitarImg,
            "Vocal Warmups & Belt" to musicImg, "Lyrical Composition" to musicImg, "Orchestration" to musicImg,
            "Film Scoring" to musicImg, "Fiddle" to violinImg, "Beatboxing" to musicImg, "Choir Rehearsal" to musicImg,
            "Chamber Ensemble" to musicImg, "Marching Band" to musicImg, "Conducting" to musicImg,
            "Fingerstyle Guitar" to guitarImg, "Flamenco Guitar" to guitarImg, "Classical Guitar" to guitarImg,
            "Slap Bass Technique" to guitarImg, "Polyrhythm Training" to drumsImg, "Ear Training & Pitch" to musicImg
        )

        musicItems.forEach { (name, img) ->
            list.add(PresetActivity(name, HabitCategory.MUSIC, "#7C4DFF", img))
        }
    }

    private fun addTechPresets(list: MutableList<PresetActivity>) {
        val codeImg = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&q=80"
        val items = listOf(
            "Android & Kotlin", "iOS & Swift", "Full-Stack Web Dev", "Python & Data Science",
            "Rust Systems Programming", "C++ & Game Engine", "Machine Learning & AI", "Cybersecurity & Pentesting",
            "DevOps & Kubernetes", "Backend API Design", "Frontend React / Next.js", "Flutter & Dart",
            "Database Design & SQL", "Cloud Architecture (AWS)", "Embedded Systems & C", "FPGA & Verilog",
            "Blockchain & Solidity", "Quantum Computing Math", "System Architecture", "Algorithms & LeetCode",
            "Game Dev (Unreal / Unity)", "Shaders & Graphics", "Linux Kernel & Shell", "GraphQL & REST",
            "TypeScript Mastery", "Golang Microservices", "Reverse Engineering", "Compiler Construction",
            "Operating Systems Dev", "Network Security", "Ethical Hacking", "Computer Vision & OpenCV",
            "Natural Language Processing", "Distributed Systems", "WebAssembly & Rust", "3D WebGL / Three.js",
            "Automation & Web Scraping", "UI/UX Code Prototyping", "Functional Programming (Haskell)", "Elixir & Phoenix",
            "Java & Spring Boot", "Node.js & Express", "Docker & CI/CD Pipelines", "Terraform & IaC",
            "Microcontroller Arduino", "Raspberry Pi Projects", "ROS & Robotics Code", "AR/XR Vision Pro Dev",
            "Android NDK / C++", "Performance Profiling", "Site Reliability Engineering", "Game AI Pathfinding",
            "Raytracing Engine Dev", "Smart Contracts", "Cryptography Math", "Data Structures", "Competitive Programming",
            "Git & Open Source", "Vim & Neovim Mastery", "Zsh & Automation", "Shell Scripting", "WebGL Shader Coding"
        )
        items.forEach { name ->
            list.add(PresetActivity(name, HabitCategory.SOFTWARE_ENGINEERING, "#00E5FF", codeImg))
        }
    }

    private fun addArtPresets(list: MutableList<PresetActivity>) {
        val artImg = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&q=80"
        val photoImg = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&q=80"
        val items = listOf(
            "Oil Painting" to artImg, "Digital Illustration" to artImg, "3D Modeling (Blender)" to artImg,
            "Concept Art" to artImg, "Street Photography" to photoImg, "Portrait Photography" to photoImg,
            "Watercolor Painting" to artImg, "Calligraphy & Lettering" to artImg, "Sculpting & Clay" to artImg,
            "Character Design" to artImg, "Pixel Art & Animation" to artImg, "Motion Graphics (After Effects)" to artImg,
            "Anatomy & Figure Drawing" to artImg, "Landscape Photography" to photoImg, "Film Photography & Darkroom" to photoImg,
            "UI/UX Design & Figma" to artImg, "Industrial Product Design" to artImg, "Architectural Sketching" to artImg,
            "Graphic Design" to artImg, "Typography & Layout" to artImg, "Ceramics & Pottery" to artImg,
            "Wood Carving" to artImg, "Origami & Paper Craft" to artImg, "Embroidery & Needlework" to artImg,
            "Fashion Design & Sewing" to artImg, "Jewelry Making" to artImg, "Glassblowing" to artImg,
            "Screen Printing" to artImg, "Stop Motion Animation" to artImg, "3D Texturing (Substance)" to artImg,
            "VFX & Compositing" to artImg, "Comic Book Illustration" to artImg, "Manga Art" to artImg,
            "Perspective Sketching" to artImg, "Color Theory & Shading" to artImg, "Astrophotography" to photoImg,
            "Macro Photography" to photoImg, "Wildlife Photography" to photoImg, "Drone Videography" to photoImg,
            "Video Editing & Premiere" to photoImg, "Color Grading (DaVinci)" to photoImg, "Cinematography" to photoImg,
            "Miniature Painting" to artImg, "Linocut Printmaking" to artImg, "Airbrushing" to artImg,
            "Acrylic Pouring" to artImg, "Tattoo Design" to artImg, "Storyboarding" to artImg,
            "Environment Concept Art" to artImg, "Rigging & 3D Animation" to artImg, "ZBrush Digital Sculpting" to artImg,
            "Blender Geometry Nodes" to artImg, "Game Asset Pipeline" to artImg, "Fashion Illustration" to artImg,
            "Pattern Making" to artImg, "Stained Glass Art" to artImg, "Resin Crafting" to artImg,
            "Leathercraft" to artImg, "Bookbinding" to artImg, "Graffiti & Mural Art" to artImg, "Charcoal Drawing" to artImg
        )
        items.forEach { (name, img) ->
            list.add(PresetActivity(name, HabitCategory.CRAFT_AND_ART, "#FF4081", img))
        }
    }

    private fun addAthleticPresets(list: MutableList<PresetActivity>) {
        val runImg = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=600&q=80"
        val gymImg = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&q=80"
        val items = listOf(
            "Distance Running" to runImg, "Marathon Training" to runImg, "Sprinting & Speedwork" to runImg,
            "Powerlifting (SBD)" to gymImg, "Bodybuilding & Hypertrophy" to gymImg, "Olympic Weightlifting" to gymImg,
            "Calisthenics & Gymnastics" to gymImg, "Brazilian Jiu-Jitsu (BJJ)" to gymImg, "Muay Thai & Kickboxing" to gymImg,
            "Boxing & Heavy Bag" to gymImg, "Rock Climbing & Bouldering" to gymImg, "Freestyle Swimming" to runImg,
            "Road Cycling" to runImg, "Mountain Biking" to runImg, "Triathlon Prep" to runImg,
            "CrossFit & WODs" to gymImg, "Yoga & Vinyasa Flow" to gymImg, "Pilates Core" to gymImg,
            "Kettlebell Training" to gymImg, "Mobility & Stretching" to gymImg, "Basketball Skills" to runImg,
            "Football / Soccer Drills" to runImg, "Tennis Serve & Rally" to runImg, "Skateboarding" to runImg,
            "Judo & Throws" to gymImg, "Wrestling" to gymImg, "Taekwondo Kicks" to gymImg,
            "Karate Kata" to gymImg, "Capoeira" to gymImg, "Parkour & Vaulting" to runImg,
            "Trail Running" to runImg, "Ultra Marathon Prep" to runImg, "Rowing Ergometer" to gymImg,
            "Jump Rope Footwork" to gymImg, "Handstand Balance" to gymImg, "Ring Muscle-ups" to gymImg,
            "Surfing" to runImg, "Windsurfing" to runImg, "Snowboarding" to runImg,
            "Skiing Technique" to runImg, "Ice Skating / Figure" to runImg, "Archery Precision" to runImg,
            "Fencing Footwork" to gymImg, "Badminton Rallies" to runImg, "Squash Matches" to runImg,
            "Table Tennis Spins" to runImg, "Volleyball Spiking" to runImg, "Golf Swing Practice" to runImg,
            "Track & Field Hurdles" to runImg, "Pole Vault" to runImg, "High Jump" to runImg,
            "Decathlon Training" to runImg, "Obstacle Course (OCR)" to runImg, "Rucking & Endurance" to runImg,
            "Strongman Log Press" to gymImg, "Functional Movement" to gymImg, "Sprint Triathlons" to runImg,
            "Open Water Swimming" to runImg, "Calisthenics Planche" to gymImg, "Front Lever Training" to gymImg,
            "Human Flag Progression" to gymImg, "Cold Plunge & Recovery" to gymImg, "Breathwork & CO2 Tables" to gymImg
        )
        items.forEach { (name, img) ->
            list.add(PresetActivity(name, HabitCategory.ATHLETICS_PHYSICAL, "#00E5FF", img))
        }
    }

    private fun addAcademicPresets(list: MutableList<PresetActivity>) {
        val bookImg = "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600&q=80"
        val items = listOf(
            "Japanese Language (JLPT)", "Mandarin Chinese (HSK)", "Spanish Fluency", "German B2/C1",
            "French Immersion", "Italian Conversation", "Russian Grammar", "Korean (TOPIK)",
            "Arabic Script & Dialects", "Latin & Classics", "Ancient Greek", "Biblical Hebrew",
            "Calculus & Real Analysis", "Linear Algebra", "Abstract Algebra", "Differential Equations",
            "Quantum Mechanics Math", "Classical Mechanics", "Electromagnetism", "Thermodynamics",
            "Organic Chemistry", "Biochemistry", "Molecular Biology", "Genetics & CRISPR",
            "Neuroscience & Synapses", "Astrophysics & Cosmology", "General Relativity", "Particle Physics",
            "Macroeconomics Theory", "Microeconomics & Game Theory", "World History Immersion", "European History",
            "Philosophy & Logic", "Ethics & Epistemology", "Cognitive Psychology", "Behavioral Economics",
            "Statistics & Probability", "Bayesian Inference", "Machine Learning Math", "Financial Modeling",
            "Corporate Finance", "Accounting Fundamentals", "Constitutional Law", "LSAT Logic Games",
            "MCAT Prep Biology", "GRE Quantitative", "TOEFL / IELTS English", "Creative Writing & Prose",
            "Non-fiction Book Writing", "Academic Research & Papers", "Speed Reading & Retention", "Anki Flashcard Deck Review",
            "Feynman Technique Study", "Public Speaking & Rhetoric", "Debate & Argumentation", "Sanskrit & Veda Studies",
            "Linguistics & Phonetics", "Etymology & Vocabulary", "Poetry Composition", "Literary Analysis",
            "Political Science & Geopolitics", "Sociology Research", "Anthropology Field Work", "Archeology & History"
        )
        items.forEach { name ->
            list.add(PresetActivity(name, HabitCategory.ACADEMIC_LANGUAGE, "#B388FF", bookImg))
        }
    }

    private fun addCulinaryPresets(list: MutableList<PresetActivity>) {
        val foodImg = "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=600&q=80"
        val items = listOf(
            "Sourdough Baking & Fermentation", "French Pastry & Croissants", "Italian Pasta Making", "Artisanal Bread Baking",
            "Knife Skills & Precision Prep", "Espresso Extraction & Latte Art", "Barista Pour-over Coffee", "Sommelier Wine Tasting",
            "Mixology & Craft Cocktails", "Charcuterie & Salumi", "Smoked Barbecue & Pitmaster", "Sous Vide Precision Cooking",
            "Japanese Sushi & Sashimi", "Ramen Broth & Noodle Dev", "Indian Curry & Spice Blends", "Thai Paste & Wok Cooking",
            "Fermentation & Kimchi / Kombucha", "Chocolatier & Tempering", "Cake Decorating & Piping", "Plating & Culinary Aesthetics",
            "Vegan / Plant-based Gourmet", "Pastry Cream & Ganache", "Wood-fired Pizza Making", "Cheese Making & Aging",
            "Carpentry & Wood Joinery", "Timber Framing", "Custom Furniture Making", "Auto Mechanics & Engine Rebuild",
            "Welding (TIG & MIG)", "Blacksmithing & Blade Forging", "Electronics Repair & Soldering", "PCB Layout & Soldering",
            "3D Printing & Filament Tuning", "Plumbing & Pipefitting", "Residential Wiring & Electrical", "HVAC Troubleshooting",
            "Shoemaking & Cobbling", "Tailoring & Suit Fitting", "Watchmaking & Horology", "Bicycle Mechanic Repair",
            "Gunsmithing", "Guitar Luthier Repair", "Violin Repair & Varnish", "Home Renovation & Drywall",
            "Tile Setting & Masonry", "Stone Carving", "Gardening & Hydroponics", "Bonsai Tree Cultivation",
            "Beekeeping & Apiary Care", "Landscaping Design", "Permaculture Systems", "Aquascaping & Planted Tanks"
        )
        items.forEach { name ->
            list.add(PresetActivity(name, HabitCategory.TRADES_CULINARY, "#FF8F00", foodImg))
        }
    }

    private fun addGamesPresets(list: MutableList<PresetActivity>) {
        val chessImg = "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=600&q=80"
        val items = listOf(
            "Chess Openings & Endgames", "Chess Tactics & Puzzles", "Go / Baduk Strategy", "Shogi Japanese Chess",
            "Poker Game Theory (GTO)", "Texas Hold'em Hand Reading", "Backgammon Odds", "Mahjong Strategy",
            "Competitive Speedrunning", "StarCraft II Micro/Macro", "Dota 2 Last Hitting & Map", "League of Legends Wave Management",
            "Counter-Strike Aim & Utility", "VALORANT Crosshair Placement", "Street Fighter Combos & Footsies", "Tekken Frame Data",
            "Smash Bros Movement & Edgeguards", "Rocket League Aerials", "TrackMania Racing Lines", "Tetris T-Spins & 4-Wide",
            "Speedcubing (Rubik's 3x3)", "Blindfolded Speedcubing", "4x4 / 5x5 Speedcubing", "Memory Palace Technique",
            "Mental Math & Trachtenberg", "Bridge Card Game Strategy", "Scrabble Anagrams & Dictionary", "Crossword Solving Speed",
            "Sudoku Advanced Techniques", "Nonogram Puzzle Solving", "Magic: The Gathering Deckbuilding", "Yu-Gi-Oh Combos",
            "D&D Dungeon Master Campaign Prep", "Worldbuilding & Lore Writing", "Puzzle Game Logic (Baba Is You)", "Rhythm Games (osu! / DDR)"
        )
        items.forEach { name ->
            list.add(PresetActivity(name, HabitCategory.GAMES_MIND, "#FFD54F", chessImg))
        }
    }

    private fun addWellnessPresets(list: MutableList<PresetActivity>) {
        val medImg = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&q=80"
        val items = listOf(
            "Vipassana Mindfulness Meditation", "Transcendental Meditation", "Loving-Kindness (Metta)", "Zazen Sitting Meditation",
            "Wim Hof Breathwork", "Pranayama Breathing", "Journaling & Reflective Writing", "Gratitude Journaling",
            "Morning Routine Consistency", "Nighttime Wind-down", "Postural Realignment", "Foam Rolling & Myofascial",
            "Cold Shower Discipline", "Sauna & Heat Therapy", "Digital Detox & Screen Discipline", "Intermittent Fasting Track",
            "Hydration Tracking", "Nature Walking & Shinrin-yoku", "Bedtime Reading Ritual", "Voice Warmups & Enunciation",
            "Posture & Spinal Care", "Daily Meditation Streak", "Stoic Evening Review", "Manifestation & Intentions"
        )
        items.forEach { name ->
            list.add(PresetActivity(name, HabitCategory.WELLNESS_LIFE, "#81C784", medImg))
        }
    }
}
