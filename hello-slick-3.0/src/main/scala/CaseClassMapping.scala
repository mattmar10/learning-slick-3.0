import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.driver.H2Driver.api._

import org.joda.time.DateTime
import java.sql.Timestamp

object CaseClassMapping extends App {

  // the base query for the Users table
  val users = TableQuery[Users]

  val db = Database.forConfig("h2mem1")
  try {
    Await.result(db.run(DBIO.seq(
      // create the schema
      users.schema.create,

      // insert two User instances
      users += User("John Doe", "john.Doe@gmail.com", Some("jDoe")),
      users += User("Fred Smith", "fred.smith@gmail.com"),

      // print the users (select * from USERS)
      users.result.map(println)
    )), Duration.Inf)
  } finally db.close
}

case class User(name: String,
                email: String,
                userName: Option[String] = None,
                birthDate: Option[DateTime] = None,
                isActive: Option[Boolean] = None,
                //isAdmin: Option[Boolean] = None,
                id: Option[Int] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  
  implicit def dateTime  =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime)
  )
  
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")
  def email = column[String]("EMAIL")
  def username = column[Option[String]]("USERNAME")

  //def firstName = column[String]("FIRST_NAME")
  //def lastName = column[String]("LAST_NAME")
  //def createdDate = column[Option[DateTime]]("CREATED_DATE", O.Default(Some(DateTime.now())))
  def birthDate = column[Option[DateTime]]("BIRTH_DAY")
  //def membershipDate = column[Option[DateTime]]("MEMBERSHIP_DATE")
  def isActive = column[Boolean]("IS_ACTIVE", O.Default(true))
  //def isAdmin = column[Option[Boolean]]("IS_ADMIN", O.Default(Some(false)))
  //def isLocked = column[Option[Boolean]]("IS_LOCKED", O.Default(Some(false)))
  //def isMember = column[Option[Boolean]]("IS_MEMBER", O.Default(Some(false)))
  //def isAttender = column[Option[Boolean]]("IS_ATTENDER", O.Default(Some(false)))

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (name, email, username, birthDate, isActive.?, id.?) <> (User.tupled, User.unapply)
}
