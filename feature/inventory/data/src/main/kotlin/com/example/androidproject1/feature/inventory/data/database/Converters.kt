package com.example.androidproject1.feature.inventory.data.database

import androidx.room.TypeConverter
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import java.time.LocalDate

/**
 * How a non-primitive column is stored: as `TEXT`, with a fallback for a name the code no longer
 * has, so a row from a later version or a hand-edited one reads as an item rather than failing
 * the list — the rule `docs/ai/RECIPES.md` § Changing a Room schema states, and `:feature:trips`
 * set. A tag set is its names joined with a comma; an unknown tag is dropped rather than mapped,
 * because there is no tag that means "one we do not know".
 */
internal class Converters {

    @TypeConverter
    fun fromCategory(category: ItemCategory): String = category.name

    @TypeConverter
    fun toCategory(name: String): ItemCategory =
        ItemCategory.entries.firstOrNull { it.name == name } ?: ItemCategory.Other

    @TypeConverter
    fun fromCondition(condition: ItemCondition): String = condition.name

    @TypeConverter
    fun toCondition(name: String): ItemCondition =
        ItemCondition.entries.firstOrNull { it.name == name } ?: ItemCondition.Good

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(text: String?): LocalDate? = text?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromTags(tags: Set<ItemTag>): String = tags.joinToString(SEPARATOR) { it.name }

    @TypeConverter
    fun toTags(text: String): Set<ItemTag> = text.split(SEPARATOR)
        .mapNotNull { name -> ItemTag.entries.firstOrNull { it.name == name } }
        .toSet()

    private companion object {

        const val SEPARATOR = ","
    }
}
