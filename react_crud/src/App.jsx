import { useEffect, useRef, useState } from 'react'

const apiUrl = (import.meta.env.VITE_API_URL || 'http://127.0.0.1:8081').replace(/\/$/, '')
const formularioVacio = { name: '', price: '', description: '' }
const filtroVacio = { tipo: 'nombre', texto: '', minimo: '', maximo: '' }

const almacenes = {
  sql: {
    etiqueta: 'SQL · MySQL',
    descripcion: 'Persistencia relacional mediante JdbcTemplate.',
    ruta: '/api/sql/productos'
  },
  mongo: {
    etiqueta: 'NoSQL · MongoDB',
    descripcion: 'Persistencia documental y consultas con aggregate.',
    ruta: '/api/mongo/productos'
  }
}

async function solicitar(ruta, opciones) {
  const respuesta = await fetch(`${apiUrl}${ruta}`, opciones)
  if (!respuesta.ok) {
    const detalle = respuesta.status === 400
      ? await respuesta.text()
      : respuesta.status === 500
        ? 'Error del backend. Revisa que la base de datos seleccionada esté activa.'
        : `Error HTTP ${respuesta.status}`
    throw new Error(detalle)
  }
  return respuesta.status === 204 ? null : respuesta.json()
}

function App() {
  const [almacen, setAlmacen] = useState('sql')
  const [productos, setProductos] = useState([])
  const [formulario, setFormulario] = useState(formularioVacio)
  const [editandoId, setEditandoId] = useState(null)
  const [filtro, setFiltro] = useState(filtroVacio)
  const [pipeline, setPipeline] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [mensaje, setMensaje] = useState('')
  const formularioRef = useRef(null)

  const configuracion = almacenes[almacen]

  async function cargarProductos({ limpiarPipeline = true } = {}) {
    setCargando(true)
    try {
      setProductos(await solicitar(configuracion.ruta))
      if (limpiarPipeline) setPipeline(null)
      setError('')
    } catch (fallo) {
      setError(fallo.message === 'Failed to fetch'
        ? 'No se pudo conectar con Spring Boot. Revisa que esté activo en el puerto 8081.'
        : fallo.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => { cargarProductos() }, [almacen])

  function seleccionarAlmacen(siguiente) {
    if (siguiente === almacen) return
    cancelar()
    setAlmacen(siguiente)
    setFiltro(filtroVacio)
    setPipeline(null)
    setProductos([])
    setError('')
    setMensaje('')
  }

  function cambiarCampo(evento) {
    setFormulario({ ...formulario, [evento.target.name]: evento.target.value })
  }

  function cambiarFiltro(evento) {
    setFiltro({ ...filtro, [evento.target.name]: evento.target.value })
  }

  function editar(producto) {
    setEditandoId(producto.id)
    setFormulario({
      name: producto.name,
      price: String(producto.price),
      description: producto.description
    })
    setMensaje('')
    setError('')
    requestAnimationFrame(() => {
      formularioRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    })
  }

  function cancelar() {
    setEditandoId(null)
    setFormulario(formularioVacio)
  }

  async function guardar(evento) {
    evento.preventDefault()
    setGuardando(true)
    setError('')
    setMensaje('')
    try {
      const producto = {
        name: formulario.name.trim(),
        price: Number(formulario.price),
        description: formulario.description.trim()
      }
      const ruta = editandoId === null ? configuracion.ruta : `${configuracion.ruta}/${editandoId}`
      await solicitar(ruta, {
        method: editandoId === null ? 'POST' : 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(producto)
      })
      setMensaje(editandoId === null
        ? `Producto creado en ${configuracion.etiqueta}.`
        : `Producto actualizado en ${configuracion.etiqueta}.`)
      cancelar()
      await cargarProductos()
    } catch (fallo) {
      setError(fallo.message)
    } finally {
      setGuardando(false)
    }
  }

  async function eliminar(producto) {
    if (!window.confirm(`¿Eliminar ${producto.name} de ${configuracion.etiqueta}?`)) return
    setError('')
    setMensaje('')
    try {
      await solicitar(`${configuracion.ruta}/${producto.id}`, { method: 'DELETE' })
      if (editandoId === producto.id) cancelar()
      setMensaje(`Producto eliminado de ${configuracion.etiqueta}.`)
      await cargarProductos()
    } catch (fallo) {
      setError(fallo.message)
    }
  }

  async function aplicarFiltro(evento) {
    evento.preventDefault()
    let ruta
    const parametros = new URLSearchParams()

    if (filtro.tipo === 'nombre' || filtro.tipo === 'descripcion') {
      if (!filtro.texto.trim()) {
        setError('Escribe el texto que deseas buscar.')
        return
      }
      ruta = `/filtros/${filtro.tipo}`
      parametros.set('texto', filtro.texto.trim())
    } else if (filtro.tipo === 'precio-minimo') {
      if (filtro.minimo === '') {
        setError('Indica un precio mínimo.')
        return
      }
      ruta = '/filtros/precio-minimo'
      parametros.set('minimo', filtro.minimo)
    } else {
      if (filtro.minimo === '' || filtro.maximo === '') {
        setError('Indica el precio mínimo y máximo.')
        return
      }
      ruta = '/filtros/rango-precio'
      parametros.set('minimo', filtro.minimo)
      parametros.set('maximo', filtro.maximo)
    }

    setCargando(true)
    setError('')
    setMensaje('')
    try {
      const resultado = await solicitar(`${almacenes.mongo.ruta}${ruta}?${parametros.toString()}`)
      setProductos(resultado.productos)
      setPipeline(resultado.pipeline)
    } catch (fallo) {
      setError(fallo.message)
    } finally {
      setCargando(false)
    }
  }

  async function limpiarFiltro() {
    setFiltro(filtroVacio)
    setMensaje('')
    await cargarProductos()
  }

  return (
    <main className="contenedor">
      <header>
        <p className="etiqueta">React + Spring Boot · Arquitectura hexagonal</p>
        <h1>CRUD SQL y NoSQL</h1>
        <p>Elige una persistencia: ambas usan el mismo contrato CRUD, pero MongoDB añade filtros con pipelines.</p>
      </header>

      <nav className="selector" aria-label="Seleccionar persistencia">
        {Object.entries(almacenes).map(([clave, opcion]) => (
          <button
            key={clave}
            type="button"
            className={almacen === clave ? 'opcion activa' : 'opcion'}
            onClick={() => seleccionarAlmacen(clave)}
            aria-pressed={almacen === clave}
          >
            <strong>{opcion.etiqueta}</strong>
            <span>{opcion.descripcion}</span>
          </button>
        ))}
      </nav>

      <section className="tarjeta contexto" aria-live="polite">
        <span>Persistencia activa</span>
        <strong>{configuracion.etiqueta}</strong>
        <code>{configuracion.ruta}</code>
      </section>

      <section ref={formularioRef} className="tarjeta" aria-labelledby="titulo-formulario">
        <h2 id="titulo-formulario">{editandoId === null ? 'Nuevo producto' : `Editar producto #${editandoId}`}</h2>
        <form onSubmit={guardar}>
          <label>Nombre
            <input name="name" value={formulario.name} onChange={cambiarCampo} required maxLength="100" />
          </label>
          <label>Precio
            <input name="price" type="number" value={formulario.price} onChange={cambiarCampo} required min="0" step="0.01" />
          </label>
          <label className="ancho">Descripción
            <input name="description" value={formulario.description} onChange={cambiarCampo} required maxLength="255" />
          </label>
          <div className="acciones ancho">
            <button type="submit" disabled={guardando}>{guardando ? 'Guardando...' : editandoId === null ? 'Crear producto' : 'Guardar cambios'}</button>
            {editandoId !== null && <button type="button" className="secundario" onClick={cancelar}>Cancelar</button>}
          </div>
        </form>
      </section>

      {almacen === 'mongo' && (
        <section className="tarjeta" aria-labelledby="titulo-filtros">
          <div className="cabecera-lista">
            <div>
              <h2 id="titulo-filtros">Filtros con MongoDB aggregate</h2>
              <p className="ayuda">Al filtrar se muestran resultados y el pipeline ejecutado.</p>
            </div>
          </div>
          <form className="filtros" onSubmit={aplicarFiltro}>
            <label>Filtro
              <select name="tipo" value={filtro.tipo} onChange={cambiarFiltro}>
                <option value="nombre">Nombre contiene</option>
                <option value="descripcion">Descripción contiene</option>
                <option value="precio-minimo">Precio mayor o igual</option>
                <option value="rango-precio">Rango de precios</option>
              </select>
            </label>
            {(filtro.tipo === 'nombre' || filtro.tipo === 'descripcion') && (
              <label>Texto
                <input name="texto" value={filtro.texto} onChange={cambiarFiltro} placeholder="Ej.: teclado" />
              </label>
            )}
            {(filtro.tipo === 'precio-minimo' || filtro.tipo === 'rango-precio') && (
              <label>Precio mínimo
                <input name="minimo" type="number" min="0" step="0.01" value={filtro.minimo} onChange={cambiarFiltro} />
              </label>
            )}
            {filtro.tipo === 'rango-precio' && (
              <label>Precio máximo
                <input name="maximo" type="number" min="0" step="0.01" value={filtro.maximo} onChange={cambiarFiltro} />
              </label>
            )}
            <div className="acciones filtro-acciones">
              <button type="submit">Aplicar filtro</button>
              <button type="button" className="secundario" onClick={limpiarFiltro}>Ver todos</button>
            </div>
          </form>
        </section>
      )}

      {pipeline && (
        <section className="tarjeta pipeline" aria-labelledby="titulo-pipeline">
          <h2 id="titulo-pipeline">Pipeline ejecutado en MongoDB</h2>
          <p>Este es el código del pipeline asociado al filtro aplicado.</p>
          <pre>{JSON.stringify(pipeline, null, 2)}</pre>
        </section>
      )}

      {error && <p role="alert" className="aviso error">{error}</p>}
      {mensaje && <p role="status" className="aviso exito">{mensaje}</p>}

      <section className="tarjeta" aria-labelledby="titulo-lista">
        <div className="cabecera-lista">
          <div>
            <h2 id="titulo-lista">Productos en {configuracion.etiqueta}</h2>
            <p className="ayuda">{pipeline ? 'Resultado del filtro actual.' : 'Listado completo de la persistencia seleccionada.'}</p>
          </div>
          <button type="button" className="secundario" onClick={() => cargarProductos()}>Actualizar lista</button>
        </div>
        {cargando ? <p>Cargando productos...</p> : (
          <div className="tabla-contenedor">
            <table>
              <thead><tr><th>ID</th><th>Nombre</th><th>Precio</th><th>Descripción</th><th>Acciones</th></tr></thead>
              <tbody>
                {productos.map((producto) => (
                  <tr key={producto.id}>
                    <td>{producto.id}</td>
                    <td>{producto.name}</td>
                    <td>{producto.price}</td>
                    <td>{producto.description}</td>
                    <td className="acciones-fila">
                      <button type="button" className="secundario" onClick={() => editar(producto)}>Editar</button>
                      <button type="button" className="peligro" onClick={() => eliminar(producto)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
                {productos.length === 0 && <tr><td colSpan="5">No hay productos para esta consulta.</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

export default App
