const numeroFormato = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });
const notaFormato = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const aporteFormato = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 5 });
const fechaFormato = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });

export const numero = (valor: number) => numeroFormato.format(valor);
export const fecha = (valor: string) => fechaFormato.format(new Date(valor));
export const aporte = (valor: number) => aporteFormato.format(valor);
/** Nota registrada con un decimal: 4,2 */
export const notaUnDecimal = (valor: number) => valor.toFixed(1).replace('.', ',');
/** Promedio con dos decimales, o raya si no hay valor. */
export const notaDosDecimales = (valor: number | null) => valor == null ? '—' : notaFormato.format(valor);
export const iniciales = (nombres: string, apellidos: string) =>
  `${nombres?.[0] ?? ''}${apellidos?.[0] ?? ''}`.toUpperCase();
/** Para buscar ignorando tildes y mayusculas. */
export const normalizar = (texto: string) =>
  texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

/**
 * Valida lo que escribe el docente: de 0 a 5 con maximo un decimal, con coma o punto.
 * Devuelve el numero o null si no es valido.
 */
export function leerNota(texto: string): number | null {
  const limpio = texto.trim().replace(',', '.');
  if (!/^\d(?:\.\d)?$/.test(limpio)) return null;
  const valor = Number(limpio);
  return valor <= 5 ? valor : null;
}
