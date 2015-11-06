//======================================
// Pak object filter
//======================================
var regExps = {
    dnt: /\\(skilltable_character.*|skillleveltable_character.*|skilltreetable|jobtable|playerleveltable|itemtable|weapontable)\.dnt$/i,
    jobicon: /^\\resource\\ui\\mainbar\\jobicon.*/i,
    skillicon: /^\\resource\\ui\\mainbar\\skillicon.*/i,
    uistring: /^\\resource\\uistring\\uistring.xml$/i,
    version: /version.cfg$/i,
    skilltree: /^\\resource\\ui\\skill\\.*\.dds/i,
}

var filter = function(node) {
    for (i in regExps) {
        if (regExps[i].test(node.path) && node.size != 0) {
            return true
        }
    }

    return false
}


//======================================
// DNT compiling
//======================================
var LEVEL_CAP = 80
var JSON_OUTPUT_DIR = "D:\\json\\" // must include trailing slash

var skills = []
var skillLevels = []
var jobs = []
var playerLevels = []
var skillTree = []
var items = []
var weapons = []
var accumulate = function(entries, cols, file) {
    var name = file.getName()
    if (name.startsWith("skilltable")) {
        skills = skills.concat(entries)
    } else if (name.startsWith("skilltreetable")) {
        skillTree = skillTree.concat(entries)
    } else if (name.startsWith("skillleveltable")) {
        skillLevels = skillLevels.concat(entries);
    } else if (name.startsWith("jobtable")) {
        jobs = jobs.concat(entries)
    } else if (name.startsWith("playerleveltable")) {
        playerLevels = playerLevels.concat(entries)
    } else if (name.startsWith("itemtable")) {
        items = items.concat(entries)
    } else if (name.startsWith("weapontable")) {
        weapons = weapons.concat(entries)
    }
}

var compile = function() {
    //================================================
    // Setup the UI String
    //================================================
    var uistring = []
    var uistringFile = new java.io.File("D:\\resource\\uistring\\uistring.xml")
    var document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uistringFile)
    document.getDocumentElement().normalize()
    var nodes = document.getElementsByTagName("message")
    for (var i = 0; i < nodes.getLength(); i++) {
        var e = nodes.item(i)
        uistring[parseInt(e.getAttribute("mid"))] = e.getFirstChild().getData()
    }

    //================================================
    // get the player levels
    //================================================
    playerLevels = playerLevels.filter(function(p) p.PrimaryID <= LEVEL_CAP).map(function(p) p.SkillPoint)
    write("levels", playerLevels)

    //================================================
    // get the weapons
    //================================================
    var weaponTypeNameIDs = {}
    items.filter(function(i) i.NameID == 1000006853 && i.LevelLimit == 1)
        .reduce(function(p,c) {
            var find = p.filter(function(_p) _p.NameIDParam == c.NameIDParam)
            if (find.length == 0) {
                p.push(c)
            }
            return p
        }, [])
        .forEach(function(i) {
            var weapon = weapons.filter(function(w) w.PrimaryID == i.PrimaryID)[0]
            weaponTypeNameIDs[weapon.EquipType] = uistring[i.NameIDParam.substring(1, i.NameIDParam.length - 1)]
        })

    write("weapons", weaponTypeNameIDs)

    //================================================
    // generate the job info, skill tree, and skills
    //================================================
    jobs.filter(function(job) job.Service).forEach(function(job) {
        // subset of the uistring
        var uistringSubset = {}

        // fix a few things
        job.MaxSPJob1 = Number(job.MaxSPJob1.toFixed(3))
        job.EnglishName = job.EnglishName.toLowerCase()

        var json = {
            EnglishName: job.EnglishName,
            JobName: uistring[job.JobName],
            JobIcon: job.JobIcon,
            SkillTree: [],
            Skills: {},
            Lookup: {},
        }

        // primary class
        if (job.JobNumber == 2) {
            var job1 = jobs.filter(function(j) j.PrimaryID == job.ParentJob)[0]
            var job0 = jobs.filter(function(j) j.PrimaryID == job1.ParentJob)[0]
            json.Set = [
                job0.EnglishName.toLowerCase(),
                job1.EnglishName.toLowerCase(),
                job.EnglishName,
            ]

            json.MaxSPJob = [
                job.MaxSPJob0,
                job.MaxSPJob1,
                job.MaxSPJob2,
            ]
        }


        // setup skill table
        jobSkills = skills.filter(function(s) s.NeedJob == job.PrimaryID)
        jobSkillsID = jobSkills.map(function(s) s.PrimaryID)
        jobSkillTree = skillTree.filter(function(t) jobSkillsID.indexOf(t.SkillTableID) > -1)
        jobSkillTreeIDs = jobSkillTree.map(function(t) t.SkillTableID)
        jobSkillTree.filter(function(t) jobSkillsID.indexOf(t.SkillTableID) > -1).forEach(function(t) {
            json.SkillTree[t.TreeSlotIndex] = t.SkillTableID

            // setup initial Skills with job sp req
            json.Skills[t.SkillTableID] = {
                NeedSP: [t.NeedBasicSP1, t.NeedFirstSP1, t.NeedSecondSP1]
            }

            var skill = json.Skills[t.SkillTableID]

            // setup the parent job hash
            if (t.ParentSkillID1 > 0) {
                skill.ParentSkills = {}
                skill.ParentSkills[t.ParentSkillID1] = t.NeedParentSkillLevel1
            }

            if (t.ParentSkillID2 > 0) {
                skill.ParentSkills[t.ParentSkillID2] = t.NeedParentSkillLevel2
            }

            if (t.ParentSkillID3 > 0) {
                skill.ParentSkills[t.ParentSkillID3] = t.NeedParentSkillLevel3
            }
        })

        // setup skill levels
        jobSkills.filter(function(s) jobSkillTreeIDs.indexOf(s.PrimaryID) > -1).forEach(function(s) {
            var levels = skillLevels.filter(function(l) l.SkillIndex == s.PrimaryID)
            var skill = json.Skills[s.PrimaryID]
            skill.NameID = s.NameID
            skill.MaxLevel = s.MaxLevel
            skill.SPMaxLevel = s.SPMaxLevel
            skill.IconImageIndex = s.IconImageIndex
            skill.SkillType = s.SkillType
            skill.Levels = {}

            uistringSubset[s.NameID] = uistring[s.NameID]

            // BaseSkillID is when two skills can't be set at same time
            if (s.BaseSkillID > 0) {
                skill.BaseSkillID = s.BaseSkillID
            }

            // weapons can be uncommon + order doesn't matter
            if (s.NeedWeaponType1 > -1 || s.NeedWeaponType2 > -1) {
                skill.NeedWeaponType = []
                if (s.NeedWeaponType1 > -1) {
                    skill.NeedWeaponType.push(s.NeedWeaponType1)
                }

                if (s.NeedWeaponType2 > -1) {
                    skill.NeedWeaponType.push(s.NeedWeaponType2)
                }
            }

            // PvE
            levels.filter(function(l) l.SkillLevel > 0 && l.SkillLevel <= s.MaxLevel).forEach(function(l) {
                if (! skill.Levels[l.SkillLevel]) {
                    skill.Levels[l.SkillLevel] = {}
                }

                level = skill.Levels[l.SkillLevel]
                var applyType = {
                    DelayTime: l.DelayTime, // cooldown
                    DecreaseSP: l.DecreaseSP, // really is MP...
                    SkillExplanationID: l.SkillExplanationID,
                    SkillExplanationIDParam: l.SkillExplanationIDParam,
                }

                if (! level.ApplyType)  {
                    level.ApplyType = []
                }

                if (l.ApplyType == 0) { // PvE
                    level.LevelLimit = l.LevelLimit // required level
                    level.SkillPoint = l.NeedSkillPoint
                    level.ApplyType[0] = applyType
                } else { // PvP
                    level.ApplyType[1] = applyType
                }

                // add uistring
                uistringSubset[l.SkillExplanationID] = uistring[l.SkillExplanationID]
                if (l.SkillExplanationIDParam) {
                    l.SkillExplanationIDParam.split(",").forEach(function(param) {
                        if (param.startsWith("{") && param.endsWith("}")) {
                            var uistringID = param.substring(1, param.length - 1)
                            uistringSubset[uistringID] = uistring[uistringID]
                        }
                    })
                }
            })
        })

        write(job.EnglishName, json)
        write("uistring_" + job.EnglishName, uistringSubset)
    })

    //================================================
    // get the map of all jobs
    //================================================
    var jobMap = {}
    jobs.filter(function(job) job.Service).forEach(function(job) {
        jobMap[job.PrimaryID] = {
            EnglishName: job.EnglishName,
            JobNumber: job.JobNumber,
            JobName: uistring[job.JobName],
            ParentJob: job.ParentJob,
        }
    })

    write("jobs", jobMap)
}

var write = function(path, json) {
    var out = new java.io.FileOutputStream(new java.io.File(JSON_OUTPUT_DIR, path + ".json"))
    out.write(JSON.stringify(json).getBytes("UTF-8"))
    out.close()
}