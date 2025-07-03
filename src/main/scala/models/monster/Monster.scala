package models.monster

class Monster(
  val name: String,
  var attributes: MonsterAttributes,
  val originZone: OriginZone,
  val monsterType: MonsterType,
  val level: Int,
  val behavior: MonsterBehavior
):
  var regenerating: Boolean = false
  var berserk: Boolean = false
  private var currentHP: Int = attributes.hp

  behavior.apply(this) // Apply directly the behavior of the monster

  def attaccaGiocatore(playerLevel: Int): Int =
    val baseDamage = attributes.attack - (playerLevel * 2)
    val damage = if (berserk) baseDamage + ((attributes.hp - currentHP) / 10) else baseDamage
    if (damage < 0) 0 else damage

  def prendiDanno(amount: Int): Unit =
    currentHP -= amount
    if behavior == Explosive && èMorto() then println(s"$name esplode!")

  def èMorto(): Boolean = currentHP <= 0

  def dropLoot(): String =
    s"${name} ha droppato un oggetto raro!"

  def rigenera(): Unit =
    if regenerating && !èMorto() then currentHP += 5

