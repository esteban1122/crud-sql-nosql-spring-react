// Ejecutar con: mongosh --file .\Spring_boot_crud\dbs\productoscrudmongo.js
// Carga datos equivalentes a los del script SQL solo si la colección está vacía.
// También deja el contador listo para nuevos productos sin borrar datos existentes.
const database = db.getSiblingDB("productoscrudmongo")

if (database.products.countDocuments() === 0) {
  database.products.insertMany([
    {
      _id: 1,
      name: "Portátil",
      price: Decimal128("2500000.00"),
      description: "Equipo de ejemplo para programación"
    },
    {
      _id: 2,
      name: "Mouse",
      price: Decimal128("45000.00"),
      description: "Mouse USB"
    },
    {
      _id: 3,
      name: "Teclado",
      price: Decimal128("85000.00"),
      description: "Teclado para el laboratorio"
    }
  ])
}

const ultimoProducto = database.products.find().sort({ _id: -1 }).limit(1).toArray()[0]
const ultimoId = ultimoProducto ? ultimoProducto._id : 0
database.counters.updateOne(
  { _id: "products_sequence" },
  { $max: { sequence: ultimoId } },
  { upsert: true }
)

database.products.find().sort({ _id: 1 })
