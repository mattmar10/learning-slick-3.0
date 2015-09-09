import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.driver.H2Driver.api._

import org.joda.time.DateTime
import java.sql.Timestamp

object CaseClassMapping extends App {

  // the base query for the Users table
  val users = TableQuery[Users]

  // Construct a query where the price of Coffees is > 9.0
  val filterQuery: Query[Users, User, Seq] =
    users.filter(_.username.isNotNull)

  val db = Database.forConfig("h2mem1")
  try {
    Await.result(db.run(DBIO.seq(
      // create the schema
      users.schema.create,

      // insert two User instances
      users += User("john.Doe@gmail.com", "jDoe", "John", None, "Doe"),
      users += User("fred.smith@gmail.com", "fSmith", "Fred", Some("Albert"), "Smith"),

      // print the users (select * from USERS)
      users.result.map(println)


    )), Duration.Inf)
    db.run(filterQuery.result.map(println))

  } finally db.close
}

case class User(email: String,
                userName: String,
                firstName: String,
                middleName: Option[String] = None,
                lastName: String,
                createdDate: Option[DateTime] = Some(DateTime.now()),
                birthDate: Option[DateTime] = None,
                membershipDate: Option[DateTime] = None,
                isMember: Option[Boolean] = Some(false),
                isActive: Option[Boolean] = Some(true),
                isAdmin: Option[Boolean] = Some(false),
                isLocked: Option[Boolean] = Some(false),
                id: Option[Int] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  
  implicit def dateTime  =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime)
  )
  
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def email = column[String]("email")
  def username = column[String]("username")

  def firstName = column[String]("first_name")
  def middleName = column[Option[String]]("middle_name")
  def lastName = column[String]("last_name")
  def createdDate = column[DateTime]("created_date")
  def birthDate = column[Option[DateTime]]("birth_day")
  def membershipDate = column[Option[DateTime]]("membership_date")
  def isMember = column[Boolean]("is_member", O.Default(false))
  def isActive = column[Boolean]("is_active", O.Default(true))
  def isAdmin = column[Boolean]("is_admin", O.Default(false))
  def isLocked = column[Boolean]("is_locked", O.Default(false))
  //def isAttender = column[Option[Boolean]]("IS_ATTENDER", O.Default(Some(false)))

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (email, username, firstName, middleName, lastName, createdDate.?, birthDate, membershipDate, isMember.?, isActive.?, isAdmin.?, isLocked.?, id.?) <> (User.tupled, User.unapply)
}
