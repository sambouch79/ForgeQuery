SELECT
  i.NOM_INDIVIDU AS nom,
  CASE
    WHEN i.STATUT = 'A' THEN 'Actif'
    WHEN i.STATUT = 'S' THEN 'Suspendu'
    WHEN i.STATUT IS NULL THEN 'Non défini'
    ELSE 'Inconnu'
  END AS statutLibelle
FROM INDIVIDU i